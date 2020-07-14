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

public class ARMStepEvent extends StepEvent {
	private final ARMCpuState state;
	private final int overrideType;

	public ARMStepEvent(ARMCpuState state) {
		this(state, -1);
	}

	public ARMStepEvent(ARMCpuState state, int overrideType) {
		super(Elf.EM_ARM, state.getTid());
		this.state = state;
		this.overrideType = overrideType;
	}

	@Override
	public byte[] getMachinecode() {
		if(Cpsr.T.getBit(state.getCPSR())) {
			byte[] machinecode = new byte[2];
			Endianess.set16bitLE(machinecode, (short) state.getCode());
			return machinecode;
		} else {
			byte[] machinecode = new byte[4];
			Endianess.set32bitLE(machinecode, state.getCode());
			return machinecode;
		}
	}

	@Override
	public String[] getDisassemblyComponents() {
		return ARMv5Disassembler.disassemble(state.getGPR(15), state.getCPSR(), state.getCode());
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
		return state.getPC();
	}

	@Override
	public InstructionType getType() {
		if(overrideType == 1) {
			return InstructionType.RET;
		} else if(overrideType == 2) {
			return InstructionType.OTHER;
		} else {
			return ARMv5Disassembler.getType(state);
		}
	}

	@Override
	public long getStep() {
		return state.getStep();
	}

	@Override
	public ARMCpuState getState() {
		return state;
	}

	@Override
	public StepFormat getFormat() {
		return ARM.FORMAT;
	}

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}
}
