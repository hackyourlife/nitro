package org.graalvm.vm.trcview.arch.arm.test;

import static org.junit.Assert.assertEquals;

import org.graalvm.vm.trcview.arch.arm.disasm.ARMv5Disassembler;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.DataProcessing;
import org.junit.Test;

public class ShifterOperandTest {
	@Test
	public void test1() {
		int value = 0x3F | (0xE << 8) | (1 << 25);
		DataProcessing fmt = new DataProcessing(value);
		String[] result = ARMv5Disassembler.shifterOperandData(fmt);
		assertEquals(1, result.length);
		assertEquals("#0x3F0", result[0]);
	}

	@Test
	public void test2() {
		int value = 0xFC | (0xF << 8) | (1 << 25);
		DataProcessing fmt = new DataProcessing(value);
		String[] result = ARMv5Disassembler.shifterOperandData(fmt);
		assertEquals(1, result.length);
		assertEquals("#0x3F0", result[0]);
	}
}
