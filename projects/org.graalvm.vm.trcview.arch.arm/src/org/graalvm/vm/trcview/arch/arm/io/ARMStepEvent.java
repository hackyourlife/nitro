package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.arm.ARM;
import org.graalvm.vm.trcview.arch.arm.disasm.ARMv5Disassembler;
import org.graalvm.vm.trcview.arch.arm.disasm.Cpsr;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.io.WordOutputStream;

public abstract class ARMStepEvent extends StepEvent {
	private int overrideType;

	protected ARMStepEvent(int tid) {
		super(Elf.EM_ARM, tid);
	}

	void setTypeOverride(int override) {
		overrideType = override;
	}

	@Override
	public byte[] getMachinecode() {
		if(Cpsr.T.getBit(getState().getCPSR())) {
			byte[] machinecode = new byte[2];
			Endianess.set16bitLE(machinecode, (short) getState().getCode());
			return machinecode;
		} else {
			byte[] machinecode = new byte[4];
			Endianess.set32bitLE(machinecode, getState().getCode());
			return machinecode;
		}
	}

	@Override
	public String[] getDisassemblyComponents() {
		return ARMv5Disassembler.disassemble(getState().getGPR(15), getState().getCPSR(), getState().getCode());
	}

	@Override
	public String getMnemonic() {
		String[] asm = getDisassemblyComponents();
		if(asm != null) {
			return asm[0];
		} else {
			return null;
		}
	}

	@Override
	public long getPC() {
		return getState().getPC();
	}

	@Override
	public InstructionType getType() {
		if(overrideType == 1) {
			return InstructionType.RET;
		} else if(overrideType == 2) {
			return InstructionType.OTHER;
		} else {
			return ARMv5Disassembler.getType(getState());
		}
	}

	@Override
	public long getStep() {
		return getState().getStep();
	}

	@Override
	public abstract ARMCpuState getState();

	@Override
	public StepFormat getFormat() {
		return ARM.FORMAT;
	}

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}
}
