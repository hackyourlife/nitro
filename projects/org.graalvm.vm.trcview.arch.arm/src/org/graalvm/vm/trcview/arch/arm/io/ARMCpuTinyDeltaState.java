package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordOutputStream;

public class ARMCpuTinyDeltaState extends ARMCpuState {
	private final ARMCpuState lastState;
	private final int cpsr;
	private final int code;
	private final long step;

	public ARMCpuTinyDeltaState(ARMCpuState state, int cpsr, int code, long step) {
		super(state.getTid());
		lastState = state;
		this.code = code;
		this.step = step;
		this.cpsr = cpsr;
	}

	@Override
	public int getGPR(int reg) {
		return lastState.getGPR(reg);
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

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}
}
