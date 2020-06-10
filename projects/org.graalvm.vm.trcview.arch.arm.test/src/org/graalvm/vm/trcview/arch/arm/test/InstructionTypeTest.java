package org.graalvm.vm.trcview.arch.arm.test;

import static org.junit.Assert.assertEquals;

import org.graalvm.vm.trcview.arch.arm.disasm.ARMv5Disassembler;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.junit.Test;

public class InstructionTypeTest {
	private static void type(int insn, int cpsr, InstructionType ref) {
		InstructionType act = ARMv5Disassembler.getType(insn, cpsr);
		assertEquals(ref, act);
	}

	@Test
	public void testSubsPCLR() {
		type(0xE25EF004, 0, InstructionType.RTI);
	}
}
