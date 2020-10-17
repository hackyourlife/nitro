package org.graalvm.vm.trcview.arch.arm.io;

public class ARMCpuTinyTargetDeltaState extends ARMCpuState {
	private final ARMCpuState lastState;
	private final int cpsr;
	private final byte id;
	private final int code;
	private final long step;
	private final int data;

	public ARMCpuTinyTargetDeltaState(ARMCpuState state, int cpsr, int code, long step, byte id, int data) {
		super(state.getTid());
		lastState = state;
		this.code = code;
		this.step = step;
		this.cpsr = cpsr;
		this.id = id;
		this.data = data;
	}

	@Override
	public int getGPR(int reg) {
		if(reg == id) {
			return data;
		} else {
			return lastState.getGPR(reg);
		}
	}

	@Override
	public int getCPSR() {
		return cpsr;
	}

	@Override
	public int getSPSR() {
		return 0;
	}

	@Override
	public int getCode() {
		return code;
	}

	@Override
	public long getStep() {
		return step;
	}
}
