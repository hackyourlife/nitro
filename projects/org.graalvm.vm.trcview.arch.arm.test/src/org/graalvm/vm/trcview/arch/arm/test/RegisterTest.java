package org.graalvm.vm.trcview.arch.arm.test;

import static org.junit.Assert.assertEquals;

import org.graalvm.vm.trcview.arch.arm.disasm.Register;
import org.junit.Test;

public class RegisterTest {
	@Test
	public void single0() {
		assertEquals("{R0}", Register.list(1));
	}

	@Test
	public void single1() {
		assertEquals("{R1}", Register.list(2));
	}

	@Test
	public void single2() {
		assertEquals("{R2}", Register.list(4));
	}

	@Test
	public void single3() {
		assertEquals("{R3}", Register.list(8));
	}

	@Test
	public void single4() {
		assertEquals("{R4}", Register.list(16));
	}

	@Test
	public void single5() {
		assertEquals("{R5}", Register.list(32));
	}

	@Test
	public void single6() {
		assertEquals("{R6}", Register.list(64));
	}

	@Test
	public void single7() {
		assertEquals("{R7}", Register.list(128));
	}

	@Test
	public void single8() {
		assertEquals("{R8}", Register.list(256));
	}

	@Test
	public void single9() {
		assertEquals("{R9}", Register.list(512));
	}

	@Test
	public void single10() {
		assertEquals("{R10}", Register.list(1024));
	}

	@Test
	public void single11() {
		assertEquals("{R11}", Register.list(2048));
	}

	@Test
	public void single12() {
		assertEquals("{R12}", Register.list(4096));
	}

	@Test
	public void single13() {
		assertEquals("{SP}", Register.list(8192));
	}

	@Test
	public void single14() {
		assertEquals("{LR}", Register.list(16384));
	}

	@Test
	public void single15() {
		assertEquals("{PC}", Register.list(32768));
	}

	@Test
	public void range0_1() {
		assertEquals("{R0-R1}", Register.list(0b0000_0000_0000_0011));
	}

	@Test
	public void range1_2() {
		assertEquals("{R1-R2}", Register.list(0b0000_0000_0000_0110));
	}

	@Test
	public void range1_10() {
		assertEquals("{R1-R10}", Register.list(0b0000_0111_1111_1110));
	}

	@Test
	public void range0_15() {
		assertEquals("{R0-R12,SP,LR,PC}", Register.list(0b1111_1111_1111_1111));
	}

	@Test
	public void range0_2_4_9_12_15() {
		assertEquals("{R0-R2,R4-R9,R12,SP,LR,PC}", Register.list(0b1111_0011_1111_0111));
	}

	@Test
	public void range0_2_4_9_13_15() {
		assertEquals("{R0-R2,R4-R9,SP,LR,PC}", Register.list(0b1110_0011_1111_0111));
	}
}
