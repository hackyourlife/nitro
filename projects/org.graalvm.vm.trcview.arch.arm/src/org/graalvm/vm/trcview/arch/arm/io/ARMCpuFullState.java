package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordOutputStream;

public class ARMCpuFullState extends ARMCpuState {
	private final int[] r = new int[16];
	private final int cpsr;
	private final int code;
	private final long step;

	protected ARMCpuFullState(ARMCpuState state) {
		super(state.getTid());
		for(int i = 0; i < 16; i++) {
			r[i] = state.getGPR(i);
		}
		cpsr = state.getCPSR();
		code = state.getCode();
		step = state.getStep();
	}

	@Override
	public int getGPR(int reg) {
		return r[reg];
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
