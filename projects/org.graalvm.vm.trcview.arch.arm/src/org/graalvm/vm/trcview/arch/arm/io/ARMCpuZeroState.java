package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.util.io.WordOutputStream;

public class ARMCpuZeroState extends ARMCpuState {
	public ARMCpuZeroState(int tid) {
		super(tid);
	}

	@Override
	public int getGPR(int reg) {
		return 0;
	}

	@Override
	public int getCPSR() {
		return 0;
	}

	@Override
	public int getSPSR() {
		return 0;
	}

	@Override
	public int getCode() {
		return 0;
	}

	@Override
	public long getStep() {
		return 0;
	}

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}
}
