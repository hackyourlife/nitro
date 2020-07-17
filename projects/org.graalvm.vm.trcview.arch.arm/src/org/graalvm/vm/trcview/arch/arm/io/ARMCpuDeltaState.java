package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.io.WordInputStream;
import org.graalvm.vm.util.io.WordOutputStream;

public class ARMCpuDeltaState extends ARMCpuState {
	private static final int MASK_CPSR = 1;

	private final ARMCpuState lastState;
	private final int cpsr;
	private final short rmask;
	private final int code;
	private final long step;
	private final int[] data;

	public ARMCpuDeltaState(ARMCpuState state, int cpsr, short rmask, int code, long step, int[] data) {
		super(state.getTid());
		lastState = state;
		this.rmask = rmask;
		this.code = code;
		this.step = step;
		this.cpsr = cpsr;
		this.data = data;
	}

	public static ARMCpuState deltaState(WordInputStream in, ARMCpuState state) throws IOException {
		byte mask = (byte) in.read8bit();
		short rmask = in.read16bit();
		int code = in.read32bit();
		long step = in.read64bit();
		int cpsr;
		if(BitTest.test(mask, MASK_CPSR)) {
			cpsr = in.read32bit();
		} else {
			cpsr = state.getCPSR();
		}

		int count = Integer.bitCount(Short.toUnsignedInt(rmask));
		if(count == 0) {
			return new ARMCpuTinyDeltaState(state, cpsr, code, step);
		} else if(count == 1) {
			int value = in.read32bit();
			return new ARMCpuSmallDeltaState(state, cpsr, code, step,
					(byte) Integer.numberOfTrailingZeros(rmask), value);
		} else {
			int[] data = new int[count];
			for(int i = 0; i < data.length; i++) {
				data[i] = in.read32bit();
			}
			return new ARMCpuDeltaState(state, cpsr, rmask, code, step, data);
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
