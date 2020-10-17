package org.graalvm.vm.trcview.arch.arm;

import java.io.InputStream;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.analysis.type.ArchitectureTypeInfo;
import org.graalvm.vm.trcview.arch.Architecture;
import org.graalvm.vm.trcview.arch.arm.decode.ARMCallDecoder;
import org.graalvm.vm.trcview.arch.arm.decode.ARMSyscallDecoder;
import org.graalvm.vm.trcview.arch.arm.io.ARMTraceReader;
import org.graalvm.vm.trcview.arch.io.ArchTraceReader;
import org.graalvm.vm.trcview.arch.io.StepFormat;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.decode.SyscallDecoder;

public class ARM extends Architecture {
	public static final short ID = Elf.EM_ARM;
	public static final StepFormat FORMAT = new StepFormat(StepFormat.NUMBERFMT_HEX, 8, 8, 1, false);

	private static final SyscallDecoder syscallDecoder = new ARMSyscallDecoder();
	private static final CallDecoder callDecoder = new ARMCallDecoder();

	@Override
	public short getId() {
		return ID;
	}

	@Override
	public String getName() {
		return "ARMv5TE";
	}

	@Override
	public String getDescription() {
		return "ARM946E-S (Nintendo DS ARM9)";
	}

	@Override
	public ArchTraceReader getTraceReader(InputStream in) {
		return new ARMTraceReader(in);
	}

	@Override
	public SyscallDecoder getSyscallDecoder() {
		return syscallDecoder;
	}

	@Override
	public CallDecoder getCallDecoder() {
		return callDecoder;
	}

	@Override
	public int getTabSize() {
		return 10;
	}

	@Override
	public StepFormat getFormat() {
		return FORMAT;
	}

	@Override
	public boolean isSystemLevel() {
		return true;
	}

	@Override
	public boolean isStackedTraps() {
		return false;
	}

	@Override
	public boolean isTaggedState() {
		return true;
	}

	@Override
	public ArchitectureTypeInfo getTypeInfo() {
		return ArchitectureTypeInfo.ILP32;
	}
}
