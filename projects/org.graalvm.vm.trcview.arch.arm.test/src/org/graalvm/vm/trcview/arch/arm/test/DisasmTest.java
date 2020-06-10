package org.graalvm.vm.trcview.arch.arm.test;

import org.junit.Test;

public class DisasmTest extends TestSupport {
	@Test
	public void data1() {
		disasm(0xE3560000, "CMP", "R6", "#0");
	}

	@Test
	public void data2() {
		disasm(0xE3E05000, "MOV", "R5", "#0xFFFFFFFF");
	}

	@Test
	public void data3() {
		disasm(0xE2855001, "ADD", "R5", "R5", "#1");
	}

	@Test
	public void data4() {
		disasm(0xE281103C, "ADD", "R1", "R1", "#0x3C");
	}

	@Test
	public void data5() {
		disasm(0xE2611000, "RSB", "R1", "R1", "#0");
	}

	@Test
	public void data6() {
		disasm(0xE1B01FA1, "MOVS", "R1", "R1", "LSR #31");
	}

	@Test
	public void data7() {
		disasm(0x03A03000, "MOVEQ", "R3", "#0");
	}

	@Test
	public void data8() {
		disasm(0xE2111502, "ANDS", "R1", "R1", "#0x800000");
	}

	@Test
	public void data9() {
		disasm(0xE2000080, "AND", "R0", "R0", "#0x80");
	}

	@Test
	public void data10() {
		disasm(0xE187740C, "ORR", "R7", "R7", "R12", "LSL #8");
	}

	@Test
	public void exception1() {
		disasm(0xE1200070, "BKPT", "#0");
	}

	@Test
	public void exception2() {
		disasm(0xE120027A, "BKPT", "#42");
	}

	@Test
	public void branch1() {
		disasm(0xE12FFF1E, "BX", "LR");
	}

	@Test
	public void branch2() {
		disasm(0xE12FFF30, "BLX", "R0");
	}

	@Test
	public void msr1() {
		disasm(0xE121F001, "MSR", "CPSR_c", "R1");
	}

	@Test
	public void msr2() {
		disasm(0xE10F1000, "MRS", "R1", "CPSR");
	}

	@Test
	public void coproc1() {
		disasm(0xEE070F9A, "MCR", "p15", "0", "R0", "c7", "c10", "4");
	}

	@Test
	public void multiply1() {
		disasm(0xE0202091, "MLA", "R0", "R1", "R0", "R2");
	}

	@Test
	public void loadstore1() {
		disasm(0xE1D0C1B8, "LDRH", "R12", "[R0, #0x18]");
	}
}
