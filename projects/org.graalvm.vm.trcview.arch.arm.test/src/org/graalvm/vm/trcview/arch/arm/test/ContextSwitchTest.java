package org.graalvm.vm.trcview.arch.arm.test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.IOException;

import org.graalvm.vm.trcview.arch.arm.io.ARMContextSwitchEvent;
import org.graalvm.vm.trcview.arch.arm.io.ARMCpuState;
import org.graalvm.vm.util.io.WordOutputStream;
import org.junit.Test;

public class ContextSwitchTest {
	private static class FakeARMCpuState extends ARMCpuState {
		public final int[] gpr = new int[16];
		public int cpsr;
		public int code;

		protected FakeARMCpuState(int code, int cpsr) {
			super(0);
			this.code = code;
			this.cpsr = cpsr;
		}

		@Override
		public int getGPR(int reg) {
			return gpr[reg];
		}

		@Override
		public int getCPSR() {
			return cpsr;
		}

		@Override
		public int getSPSR() {
			return cpsr;
		}

		@Override
		public int getCode() {
			return code;
		}

		@Override
		public long getStep() {
			return 0;
		}

		@Override
		protected void writeRecord(WordOutputStream out) throws IOException {
		}
	}

	@Test
	public void noContextSwitchInIRQ() {
		ARMCpuState state = new FakeARMCpuState(0xE82D500F, 0x600000d2);
		assertFalse(ARMContextSwitchEvent.isContextSwitch(state));
	}

	@Test
	public void contextSwitchInIRQ() {
		ARMCpuState state = new FakeARMCpuState(0xE9F17FFF, 0x600000d2);
		assertTrue(ARMContextSwitchEvent.isContextSwitch(state));
	}

	@Test
	public void contextSave() {
		ARMCpuState state = new FakeARMCpuState(0xE8817FFF, 0x600000d2);
		assertTrue(ARMContextSwitchEvent.isContextSave(state));
	}

	@Test
	public void contextSaveInIRQ() {
		ARMCpuState state = new FakeARMCpuState(0xE9E07FFC, 0x600000d2);
		assertTrue(ARMContextSwitchEvent.isContextSave(state));
	}
}
