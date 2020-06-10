package org.graalvm.vm.trcview.arch.arm.io;

import java.io.EOFException;
import java.io.IOException;
import java.io.InputStream;

import org.graalvm.vm.posix.api.mem.Mman;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.Event;
import org.graalvm.vm.trcview.arch.io.MemoryDumpEvent;
import org.graalvm.vm.trcview.arch.io.MemoryEvent;
import org.graalvm.vm.trcview.arch.io.MmapEvent;
import org.graalvm.vm.trcview.net.protocol.IO;
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

	private final WordInputStream in;
	private ARMStepEvent lastStep;
	private ARMCpuState lastState = new ARMCpuZeroState(0);
	private long steps = 0;
	private int init = 0;

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
			return mmap(0x01000000, k(0x01000000, 32), "ITCM");
		case 1:
			init++;
			return mmap(0x02000000, meg(0x02000000, 4), "Main Memory");
		case 2:
			init++;
			return mmap(0x027C0000, k(0x027C0000, 16), "DTCM");
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
			lastState = new ARMCpuDeltaState(in, lastState);
			if(steps >= STEP_LIMIT) {
				lastState = new ARMCpuFullState(lastState);
				steps = 0;
			}
			lastStep = new ARMStepEvent(lastState);
			return lastStep;
		case TYPE_READ_8: {
			long value = Integer.toUnsignedLong(in.read8bit());
			long address = Integer.toUnsignedLong(in.read32bit());
			byte size = 1;
			return new MemoryEvent(false, 0, address, size, false, value);
		}
		case TYPE_READ_16: {
			long value = Integer.toUnsignedLong(in.read16bit());
			long address = Integer.toUnsignedLong(in.read32bit());
			byte size = 2;
			return new MemoryEvent(false, 0, address, size, false, value);
		}
		case TYPE_READ_32: {
			long value = Integer.toUnsignedLong(in.read32bit());
			long address = Integer.toUnsignedLong(in.read32bit());
			byte size = 4;
			return new MemoryEvent(false, 0, address, size, false, value);
		}
		case TYPE_WRITE_8: {
			long value = Integer.toUnsignedLong(in.read8bit());
			long address = Integer.toUnsignedLong(in.read32bit());
			byte size = 1;
			return new MemoryEvent(false, 0, address, size, true, value);
		}
		case TYPE_WRITE_16: {
			long value = Integer.toUnsignedLong(in.read16bit());
			long address = Integer.toUnsignedLong(in.read32bit());
			byte size = 2;
			return new MemoryEvent(false, 0, address, size, true, value);
		}
		case TYPE_WRITE_32: {
			long value = Integer.toUnsignedLong(in.read32bit());
			long address = Integer.toUnsignedLong(in.read32bit());
			byte size = 4;
			return new MemoryEvent(false, 0, address, size, true, value);
		}
		case TYPE_DUMP: {
			long address = Integer.toUnsignedLong(in.read32bit());
			byte[] data = IO.readArray(in);
			return new MemoryDumpEvent(0, address, data);
		}
		case TYPE_IRQ: {
			return new ARMExceptionEvent(0, lastStep);
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
