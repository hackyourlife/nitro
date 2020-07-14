package org.graalvm.vm.trcview.arch.arm.test;

import static org.junit.Assert.assertArrayEquals;

import org.graalvm.vm.trcview.arch.arm.disasm.ARMv5Disassembler;

public class TestSupport {
	private static String str(String[] ref) {
		return String.join(", ", ref);
	}

	protected void disasm(int pc, int op, String... ref) {
		String[] act = ARMv5Disassembler.disassemble(pc + 8, 0, op);
		assertArrayEquals("ref=" + str(ref) + " act=" + str(act), ref, act);
	}

	protected void disasm(int op, String... ref) {
		String[] act = ARMv5Disassembler.disassemble(0x02000000, 0, op);
		assertArrayEquals("ref=" + str(ref) + " act=" + str(act), ref, act);
	}
}
