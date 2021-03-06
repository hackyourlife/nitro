package org.graalvm.vm.trcview.arch.arm.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.arch.arm.device.ARMDevices;
import org.graalvm.vm.trcview.arch.arm.disasm.ARMv5Disassembler;
import org.graalvm.vm.trcview.arch.arm.disasm.Cpsr;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.Thumb;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEventI16;
import org.graalvm.vm.trcview.arch.io.MemoryEventI32;
import org.graalvm.vm.trcview.arch.io.MemoryEventI8;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.net.protocol.IO;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;
import org.graalvm.vm.util.io.LEInputStream;
import org.graalvm.vm.util.io.WordInputStream;

public class ARMTraceReader extends ArchTraceReader {
	public static final int TYPE_STEP7 = 0;
	public static final int TYPE_STEP9 = 1;
	public static final int TYPE_READ_8 = 2;
	public static final int TYPE_READ_16 = 3;
	public static final int TYPE_READ_32 = 4;
	public static final int TYPE_WRITE_8 = 5;
	public static final int TYPE_WRITE_16 = 6;
	public static final int TYPE_WRITE_32 = 7;
	public static final int TYPE_DUMP = 8;
	public static final int TYPE_IRQ = 9;

	private static final int STEP_LIMIT = 5_000;

	private final Thumb thumb = new Thumb();

	private final WordInputStream in;
	private ARMStepEvent lastStep;
	private ARMCpuState lastState = new ARMCpuZeroState(0);
	private ARMContextSwitchEvent contextSwitch = null;
	private boolean contextSwitchCommitted = false;
	private MemoryEvent mem = null;
	private long steps = 0;
	private int init = 0;

	private Map<Integer, Integer> threads = new HashMap<>();

	private int tid = 0;
	private int tidcnt = 1;
	private ARMCpuState lastIRQState;

	public ARMTraceReader(InputStream in) {
		this.in = new LEInputStream(in);
		lastStep = null;
	}

	private static MmapEvent mmap(int start, int end, String name) {
		return new MmapEvent(0, Integer.toUnsignedLong(start), Integer.toUnsignedLong(end - start + 1),
				Mman.PROT_READ | Mman.PROT_WRITE | Mman.PROT_EXEC,
				Mman.MAP_FIXED | Mman.MAP_PRIVATE | Mman.MAP_ANONYMOUS, -1, 0, name,
				Integer.toUnsignedLong(start), null);
	}

	private static int k(int base, int size) {
		return base + size * 1024 - 1;
	}

	private static int meg(int base, int size) {
		return k(base, size * 1024);
	}

	@Override
	public Event read() throws IOException {
		switch(init) {
		case 0:
			init++;
			return mmap(0x01000000, meg(0x01000000, 32), "ITCM");
		case 1:
			init++;
			return mmap(0x02000000, meg(0x02000000, 4), "Main Memory");
		case 2:
			init++;
			return mmap(0x027C0000, 0x027FFFFF, "DTCM");
		case 3:
			init++;
			return mmap(0x027FF000, k(0x027FF000, 4), "Shared Work");
		case 4:
			init++;
			return mmap(0x04000000, 0x04FFFFFF, "I/O Ports");
		case 5:
			init++;
			return mmap(0x05000000, k(0x05000000, 2), "Standard Palettes");
		case 6:
			init++;
			return mmap(0x06000000, k(0x06000000, 512), "VRAM Engine A (BG)");
		case 7:
			init++;
			return mmap(0x06200000, k(0x06200000, 128), "VRAM Engine B (BG)");
		case 8:
			init++;
			return mmap(0x06400000, k(0x06400000, 256), "VRAM Engine A (OBJ)");
		case 9:
			init++;
			return mmap(0x06600000, k(0x06600000, 128), "VRAM Engine B (OBJ)");
		case 10:
			init++;
			return mmap(0x06800000, k(0x06800000, 656), "VRAM (LCD)");
		case 11:
			init++;
			return mmap(0x07000000, k(0x07000000, 2), "OAM");
		case 12:
			init++;
			return mmap(0x08000000, meg(0x08000000, 32), "GBA Slot ROM");
		case 13:
			init++;
			return mmap(0x0A000000, k(0x0A000000, 64), "GBA Slot RAM");
		case 14:
			init++;
			return mmap(0xFFFF0000, k(0xFFFF0000, 32), "BIOS");
		case 15:
			init++;
			return ARMDevices.createDevices();
		}

		if(contextSwitch != null && !contextSwitchCommitted) {
			contextSwitchCommitted = true;
			return contextSwitch;
		}

		if(mem != null) {
			Event evt = ARMDevices.getEvent(mem);
			mem = null;
			if(evt != null) {
				return evt;
			}
		}

		byte type;
		try {
			type = (byte) in.read8bit();
		} catch(EOFException e) {
			return null;
		}
		switch(type) {
		case TYPE_STEP9:
			steps++;
			lastState = ARMCpuDeltaState.deltaState(in, lastState);
			if(steps >= STEP_LIMIT) {
				lastState = new ARMCpuFullState(lastState);
				steps = 0;
			}

			// detect POP R2; BX R2 in ARM9 BIOS code and interpret it as return
			if(lastStep != null && Cpsr.T.getBit(lastStep.getState().getCPSR()) &&
					Cpsr.T.getBit(lastState.getCPSR())) {
				// was in Thumb mode, and still is in Thumb mode
				// now check if the last instruction was a POP and the current one is a BX
				int code = lastState.getCode();
				if((code & 0b1111111110000000) == 0b0100011100000000) {
					// this is a BX, so let's get the register
					int reg = thumb.Rn.get(code);
					// now let's see if the previous instruction was a POP with this register
					int lastCode = lastStep.getState().getCode();
					if((lastCode & 0b1111111100000000) == 0b1011110000000000) {
						// this was a POP without PC, now check the register
						if(BitTest.test(lastCode, 1 << reg)) {
							// yes, the register was POP'd
							// override the instruction type to RET
							lastStep = lastState;
							lastStep.setTypeOverride(1);
							return lastStep;
						}
					}
				}
			}

			// detect context switches
			if(ARMContextSwitchEvent.isContextSwitch(lastState)) {
				// ignore previous context switch; this is necessary for OS_RescheduleThreads followed
				// by a reschedule IRQ
				contextSwitch = new ARMContextSwitchEvent(0,
						Cpsr.M.get(lastState.getCPSR()) == 0b10010);
				contextSwitchCommitted = false;
			} else if(ARMContextSwitchEvent.isContextSave(lastState)) {
				// context save: store current thread ID for later matching
				int thread = ARMContextSwitchEvent.getThreadID(lastState, lastIRQState);
				threads.put(thread, tid);
			} else if(contextSwitch != null && Cpsr.M.get(lastState.getCPSR()) != 0b10011 &&
					Cpsr.M.get(lastState.getCPSR()) != 0b10010) {
				// determine new thread id after context switch
				int thread = ARMContextSwitchEvent.getThreadID(lastState);
				if(threads.containsKey(thread)) {
					tid = threads.get(thread);
				} else {
					tid = tidcnt++;
					threads.put(thread, tid);
				}
				contextSwitch.setThreadID(tid);
				contextSwitch.setThreadAddress(thread);
				contextSwitch = null;
			}

			if(lastState.getTid() != tid) {
				lastState = new ARMCpuFullState(lastState, tid);
				if(ARMv5Disassembler.getType(lastState) == InstructionType.RET) {
					// no RET after context switch
					lastStep = lastState;
					lastStep.setTypeOverride(2);
				} else {
					lastStep = lastState;
				}
			} else {
				lastStep = lastState;
			}
			return lastStep;
		case TYPE_READ_8: {
			byte value = (byte) in.read8bit();
			long address = Integer.toUnsignedLong(in.read32bit());
			return mem = new MemoryEventI8(false, tid, address, false, value);
		}
		case TYPE_READ_16: {
			short value = in.read16bit();
			long address = Integer.toUnsignedLong(in.read32bit());
			return mem = new MemoryEventI16(false, tid, address, false, value);
		}
		case TYPE_READ_32: {
			int value = in.read32bit();
			long address = Integer.toUnsignedLong(in.read32bit());
			return mem = new MemoryEventI32(false, tid, address, false, value);
		}
		case TYPE_WRITE_8: {
			byte value = (byte) in.read8bit();
			long address = Integer.toUnsignedLong(in.read32bit());
			return mem = new MemoryEventI8(false, tid, address, true, value);
		}
		case TYPE_WRITE_16: {
			short value = in.read16bit();
			long address = Integer.toUnsignedLong(in.read32bit());
			return mem = new MemoryEventI16(false, tid, address, true, value);
		}
		case TYPE_WRITE_32: {
			int value = in.read32bit();
			long address = Integer.toUnsignedLong(in.read32bit());
			return mem = new MemoryEventI32(false, tid, address, true, value);
		}
		case TYPE_DUMP: {
			long address = Integer.toUnsignedLong(in.read32bit());
			byte[] data = IO.readArray(in);
			return new MemoryDumpEvent(tid, address, data);
		}
		case TYPE_IRQ: {
			if(lastStep != null) {
				lastIRQState = lastStep.getState();
			}
			return new ARMExceptionEvent(tid, lastStep);
		}
		default:
			throw new IOException("unknown record: " + HexFormatter.tohex(type, 8) +
					" [position " + tell() + "]");
		}
	}

	@Override
	public long tell() {
		return in.tell();
	}
}
