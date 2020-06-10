package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;
import org.graalvm.vm.util.io.WordOutputStream;

public class ARMExceptionEvent extends InterruptEvent {
	private final ARMStepEvent step;

	public ARMExceptionEvent(int tid, ARMStepEvent step) {
		super(Elf.EM_ARM, tid);
		this.step = step;
	}

	@Override
	public StepEvent getStep() {
		return step;
	}

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public String toString() {
		return "IRQ";
	}
}
