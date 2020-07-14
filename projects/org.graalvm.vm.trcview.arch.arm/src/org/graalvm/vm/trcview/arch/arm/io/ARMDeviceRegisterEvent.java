package org.graalvm.vm.trcview.arch.arm.io;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import org.graalvm.vm.trcview.analysis.device.RegisterValue;
import org.graalvm.vm.trcview.arch.arm.ARM;
import org.graalvm.vm.trcview.arch.io.DeviceRegisterEvent;
import org.graalvm.vm.util.io.WordOutputStream;

public class ARMDeviceRegisterEvent extends DeviceRegisterEvent {
	private final int dev;
	private final int reg;
	private final int value;
	private final boolean read;
	private final boolean write;

	public ARMDeviceRegisterEvent(int tid, int dev, int reg, int value) {
		super(ARM.ID, tid);
		this.dev = dev;
		this.reg = reg;
		this.value = value;
		this.write = false;
		this.read = false;
	}

	public ARMDeviceRegisterEvent(int tid, int dev, int reg, int value, boolean write) {
		super(ARM.ID, tid);
		this.dev = dev;
		this.reg = reg;
		this.value = value;
		this.write = write;
		this.read = !write;
	}

	@Override
	public int getDeviceId() {
		return dev;
	}

	@Override
	public List<RegisterValue> getValues() {
		return Collections.singletonList(new RegisterValue(reg, Integer.toUnsignedLong(value)));
	}

	@Override
	public List<RegisterValue> getWrites() {
		if(write) {
			return getValues();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	public List<RegisterValue> getReads() {
		if(read) {
			return getValues();
		} else {
			return Collections.emptyList();
		}
	}

	@Override
	protected void writeRecord(WordOutputStream out) throws IOException {
		// TODO Auto-generated method stub
	}
}
