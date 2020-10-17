package org.graalvm.vm.trcview.arch.arm.io;

import org.graalvm.vm.trcview.arch.io.InterruptEvent;
import org.graalvm.vm.trcview.arch.io.StepEvent;

public class ARMExceptionEvent extends InterruptEvent {
	private final ARMStepEvent step;

	public ARMExceptionEvent(int tid, ARMStepEvent step) {
		super(tid);
		this.step = step;
	}

	@Override
	public StepEvent getStep() {
		return step;
	}

	@Override
	public String toString() {
		return "IRQ";
	}
}
