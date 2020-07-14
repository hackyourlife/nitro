package org.graalvm.vm.trcview.arch.arm.device.event.ge;

import org.graalvm.vm.trcview.analysis.device.FieldNumberType;
import org.graalvm.vm.trcview.analysis.device.IntegerFieldFormat;
import org.graalvm.vm.trcview.arch.arm.device.ARMDevices;

public class G3Viewport extends G3Event {
	private static final IntegerFieldFormat x1 = new IntegerFieldFormat("X1", 7, 0, FieldNumberType.DEC);
	private static final IntegerFieldFormat y1 = new IntegerFieldFormat("Y1", 15, 8, FieldNumberType.DEC);
	private static final IntegerFieldFormat x2 = new IntegerFieldFormat("X2", 23, 16, FieldNumberType.DEC);
	private static final IntegerFieldFormat y2 = new IntegerFieldFormat("Y2", 31, 24, FieldNumberType.DEC);

	public G3Viewport(int tid, int value) {
		super(tid, ARMDevices.VIEWPORT, value);
	}

	@Override
	public String getMessage() {
		return "VIEWPORT [" + format(x1) + ", " + format(y1) + ", " + format(x2) + ", " + format(y2) + "]";
	}
}
