package org.graalvm.vm.trcview.arch.arm.decode;

import org.graalvm.vm.trcview.arch.arm.disasm.Cpsr;
import org.graalvm.vm.trcview.arch.arm.io.ARMCpuState;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.decode.SyscallDecoder;
import org.graalvm.vm.trcview.net.TraceAnalyzer;
import org.graalvm.vm.util.HexFormatter;

public class ARMSyscallDecoder extends SyscallDecoder {
	public static final String[] SYSCALL_NAMES = {
			/* 00 */ "SoftReset",
			/* 01 */ null,
			/* 02 */ null,
			/* 03 */ "WaitByLoop",
			/* 04 */ "IntrWait",
			/* 05 */ "VBlankIntrWait",
			/* 06 */ "Halt",
			/* 07 */ null,
			/* 08 */ null,
			/* 09 */ "Div",
			/* 0A */ null,
			/* 0B */ "CpuSet",
			/* 0C */ "CpuFastSet",
			/* 0D */ "Sqrt",
			/* 0E */ "GetCRC16",
			/* 0F */ "IsDebugger",
			/* 10 */ "BitUnPack",
			/* 11 */ "LZ77UnCompReadNormalWrite8bit",
			/* 12 */ "LZ77UnCompReadByCallbackWrite16bit",
			/* 13 */ "HuffUnCompReadByCallback",
			/* 14 */ "RLUnCompReadNormalWrite8bit",
			/* 15 */ "RLUnCompReadByCallbackWrite16bit",
			/* 16 */ "Diff8bitUnFilterWrite8bit",
			/* 17 */ null,
			/* 18 */ "Diff16bitUnFilter",
			/* 19 */ null,
			/* 1A */ null,
			/* 1B */ null,
			/* 1C */ null,
			/* 1D */ null,
			/* 1E */ null,
			/* 1F */ "CustomPost"
	};

	private static final String ptr(int x) {
		if(x == 0) {
			return "NULL";
		} else {
			return "0x" + HexFormatter.tohex(Integer.toUnsignedLong(x));
		}
	}

	private static final String hex(int x) {
		long z = Integer.toUnsignedLong(x);
		if(z < 9) {
			return Long.toString(z);
		} else {
			return "0x" + HexFormatter.tohex(z);
		}
	}

	public String decode(ARMCpuState state) {
		int id;
		if(Cpsr.T.getBit(state.getCPSR())) {
			// Thumb mode
			id = state.getCode() & 0xFF;
		} else {
			id = (state.getCode() >> 16) & 0xFF;
		}
		switch(id) {
		case 0x00:
			return "SoftReset()";
		case 0x03:
			return "WaitByLoop(" + hex(state.getGPR(0)) + ")";
		case 0x04:
			return "IntrWait(" + hex(state.getGPR(0)) + ", " + hex(state.getGPR(1)) + ", " +
					hex(state.getGPR(2)) + ")";
		case 0x05:
			return "VBlankIntrWait()";
		case 0x06:
			return "Halt()";
		case 0x09:
			return "Div(" + state.getGPR(0) + ", " + state.getGPR(1) + ")";
		case 0x0B:
			return "CpuSet(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) + ", " +
					hex(state.getGPR(2)) + ")";
		case 0x0C:
			return "CpuSetFast(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) + ", " +
					hex(state.getGPR(2)) + ")";
		case 0x0D:
			return "Sqrt(" + Integer.toUnsignedLong(state.getGPR(0)) + ")";
		case 0x0E:
			return "GetCRC16(" + hex(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) + ", " +
					Integer.toUnsignedString(state.getGPR(2)) + ")";
		case 0x0F:
			return "IsDebugger()";
		case 0x10:
			return "BitUnPack(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) + ", " +
					ptr(state.getGPR(2)) + ")";
		case 0x11:
			return "LZ77UnCompReadNormalWrite8bit(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) +
					", " + ptr(state.getGPR(2)) + ", " + ptr(state.getGPR(3)) + ")";
		case 0x12:
			return "LZ77UnCompReadByCallbackWrite16bit(" + ptr(state.getGPR(0)) + ", " +
					ptr(state.getGPR(1)) + ", " + hex(state.getGPR(2)) + ", " +
					ptr(state.getGPR(3)) + ")";
		case 0x13:
			return "HuffUnCompReadByCallback(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) + ", " +
					hex(state.getGPR(2)) + ", " + ptr(state.getGPR(3)) + ")";
		case 0x14:
			return "RLUnCompReadNormalWrite8bit(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) +
					", " + hex(state.getGPR(2)) + ", " + ptr(state.getGPR(3)) + ")";
		case 0x15:
			return "RLUnCompReadByCallbackWrite16bit(" + ptr(state.getGPR(0)) + ", " +
					ptr(state.getGPR(1)) + ", " + hex(state.getGPR(2)) + ", " +
					ptr(state.getGPR(3)) + ")";
		case 0x16:
			return "Diff8bitUnFilterWrite8bit(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) + ")";
		case 0x18:
			return "Diff16bitUnFilter(" + ptr(state.getGPR(0)) + ", " + ptr(state.getGPR(1)) + ")";
		case 0x1F:
			return "CustomPort(" + hex(state.getGPR(0)) + ")";
		}
		if(id < SYSCALL_NAMES.length) {
			return SYSCALL_NAMES[id];
		}
		return null;
	}

	@Override
	public String decode(CpuState state, CpuState next, TraceAnalyzer trc) {
		return decode((ARMCpuState) state);
	}

	@Override
	public String decodeResult(int sc, CpuState state) {
		return null;
	}

	@Override
	public String decode(CpuState state, TraceAnalyzer trc) {
		return decode((ARMCpuState) state);
	}
}
