package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class ARMCpuDeltaState extends ARMCpuState {
	private static final int MASK_CPSR = 1;

	private final ARMCpuState lastState;
	private final int cpsr;
	private final byte mask;
	private final short rmask;
	private final int code;
	private final long step;
	private final int[] data;

	public ARMCpuDeltaState(WordInputStream in, ARMCpuState state) throws IOException {
		super(state.getTid());
		lastState = state;
		mask = (byte) in.read8bit();
		rmask = in.read16bit();
		code = in.read32bit();
		step = in.read64bit();
		if(BitTest.test(mask, MASK_CPSR)) {
			cpsr = in.read32bit();
		} else {
			cpsr = state.getCPSR();
		}
		int count = Integer.bitCount(Short.toUnsignedInt(rmask));
		data = new int[count];
		for(int i = 0; i < data.length; i++) {
			data[i] = in.read32bit();
		}
	}

	@Override
	public int getGPR(int reg) {
		if(BitTest.test(rmask, 1 << reg)) {
			int off = 0;
			for(int i = 0; i < reg; i++) {
				if(BitTest.test(rmask, 1 << i)) {
					off++;
				}
			}
			return data[off];
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

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}
}
