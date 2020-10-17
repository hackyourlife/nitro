package org.graalvm.vm.trcview.arch.arm.io;

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
}
