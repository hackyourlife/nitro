package org.graalvm.vm.trcview.arch.arm.device.event.ge;

import org.graalvm.vm.trcview.arch.arm.device.ARMDevices;
import org.graalvm.vm.util.BitTest;

public class G3SwapBuffers extends G3Event {
	public G3SwapBuffers(int tid, int value) {
		super(tid, ARMDevices.SWAP_BUFFERS, value);
	}

	@Override
	public String getMessage() {
		String sort = BitTest.test(value, 1) ? "manual-sort" : "auto-sort";
		String depth = BitTest.test(value, 2) ? "W-value" : "Z-value";
		return "SWAP_BUFFERS [Y-sorting: " + sort + ", depth buffering: " + depth + "]";
	}
}
