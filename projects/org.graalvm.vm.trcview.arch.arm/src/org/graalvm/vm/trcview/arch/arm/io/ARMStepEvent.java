package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.arm.ARM;
import org.graalvm.vm.trcview.arch.arm.disasm.ARMv5Disassembler;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.util.io.Endianess;
import org.graalvm.vm.util.io.WordOutputStream;

public class ARMStepEvent extends StepEvent {
	private final ARMCpuState state;

	public ARMStepEvent(ARMCpuState state) {
		super(Elf.EM_ARM, state.getTid());
		this.state = state;
	}

	@Override
	public byte[] getMachinecode() {
		byte[] machinecode = new byte[4];
		Endianess.set32bitLE(machinecode, state.getCode());
		return machinecode;
	}

	@Override
	public String[] getDisassemblyComponents() {
		return ARMv5Disassembler.disassemble((int) state.getPC(), state.getCode());
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
		return state.getPC() - 8;
	}

	@Override
	public InstructionType getType() {
		return ARMv5Disassembler.getType(state);
	}

	@Override
	public long getStep() {
		return state.getStep();
	}

	@Override
	public CpuState getState() {
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
