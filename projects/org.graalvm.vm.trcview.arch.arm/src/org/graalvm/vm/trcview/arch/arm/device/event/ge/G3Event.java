package org.graalvm.vm.trcview.arch.arm.device.event.ge;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.FieldFormat;
import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.arm.ARM;
import org.graalvm.vm.trcview.arch.arm.device.ARMDevices;
import org.graalvm.vm.trcview.arch.io.DeviceEvent;
import org.graalvm.vm.util.io.WordOutputStream;

public abstract class G3Event extends DeviceEvent {
	protected final int value;
	protected final int reg;

	protected G3Event(int tid, int reg, int value) {
		super(ARM.ID, tid);
		this.reg = reg;
		this.value = value;
	}

	@Override
	public int getDeviceId() {
		return ARMDevices.GE;
	}

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}

	@Override
	public List<RegisterValue> getValues() {
		return Collections.singletonList(new RegisterValue(reg, Integer.toUnsignedLong(value)));
	}

	@Override
	public List<RegisterValue> getWrites() {
		return getValues();
	}

	protected String format(FieldFormat fmt) {
		return fmt.getName() + " = " + fmt.format(Integer.toUnsignedLong(value));
	}
}
