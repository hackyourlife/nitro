package org.graalvm.vm.trcview.arch.arm.io;

import org.graalvm.vm.trcview.arch.arm.device.ARMDevices;
import org.graalvm.vm.trcview.arch.arm.disasm.ARMv5Disassembler;
import org.graalvm.vm.trcview.arch.arm.disasm.Cpsr;
import org.graalvm.vm.trcview.arch.io.DeviceEvent;

public class ARMContextSwitchEvent extends DeviceEvent {
	private int id;
	private int address;
	private final boolean preempt;

	public ARMContextSwitchEvent(int tid, boolean preempt) {
		super(tid);
		this.id = 0;
		this.address = 0;
		this.preempt = preempt;
	}

	public static boolean isContextSwitch(ARMCpuState state) {
		int cpsr = state.getCPSR();
		int mode = Cpsr.M.get(cpsr);
		if(mode == 0b10011) {
			// in supervisor mode
			int code = state.getCode();
			if((code & 0b00001110011100001_111111111111111) == 0b00001000010100000_111111111111111) {
				return ARMv5Disassembler.conditionPassed(code, cpsr);
			} else {
				return false;
			}
		} else if(mode == 0b10010) {
			// in IRQ mode
			int code = state.getCode();
			if((code & 0b00001110010100000_111111111111111) == 0b00001000010100000_111111111111111) {
				return ARMv5Disassembler.conditionPassed(code, cpsr);
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

	public static boolean isContextSave(ARMCpuState state) {
		int code = state.getCode();
		if((code & 0b0000_11100101_0000_1111111111111111) == 0b0000100000000000_0111111111111111) {
			return ARMv5Disassembler.conditionPassed(code, state.getCPSR());
		} else if((code & 0b0000_11100101_0000_1111111111111100) == 0b0000100001000000_0111111111111100) {
			// preemptive scheduler: saves R0-R1 separately
			return ARMv5Disassembler.conditionPassed(code, state.getCPSR());
		}
		return false;
	}

	public static int getThreadID(ARMCpuState state) {
		int mode = Cpsr.M.get(state.getCPSR());
		if(mode == 0b10011) {
			// supervisor mode
			throw new IllegalArgumentException("cannot determine thread ID in supervisor mode");
		} else if(mode == 0b10010) {
			// IRQ mode
			throw new IllegalArgumentException("cannot determine thread ID in IRQ mode");
		}

		return state.getGPR(13);
	}

	public static int getThreadID(ARMCpuState state, ARMCpuState lastIRQState) {
		int mode = Cpsr.M.get(state.getCPSR());
		if(mode == 0b10011) {
			// supervisor mode
			throw new IllegalArgumentException("cannot determine thread ID in supervisor mode");
		} else if(mode == 0b10010) {
			return lastIRQState.getGPR(13);
		}

		return state.getGPR(13);
	}

	void setThreadID(int id) {
		this.id = id;
	}

	void setThreadAddress(int address) {
		this.address = address;
	}

	@Override
	public int getDeviceId() {
		return ARMDevices.CPU;
	}

	@Override
	public String getMessage() {
		if(preempt) {
			return "Context switch: execute thread " + id + " [0x" + Integer.toUnsignedString(address, 16) +
					", IRQ]";
		} else {
			return "Context switch: execute thread " + id + " [0x" + Integer.toUnsignedString(address, 16) +
					"]";
		}
	}
}
