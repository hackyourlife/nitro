package org.graalvm.vm.trcview.arch.arm.io;

import org.graalvm.vm.posix.elf.Elf;
import org.graalvm.vm.trcview.arch.arm.disasm.Cpsr;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.util.HexFormatter;

public abstract class ARMCpuState extends CpuState {
	protected ARMCpuState(int tid) {
		super(Elf.EM_ARM, tid);
	}

	public abstract int getGPR(int reg);

	public abstract int getCPSR();

	public abstract int getSPSR();

	public abstract int getCode();

	@Override
	public long getPC() {
		return Integer.toUnsignedLong(getGPR(15));
	}

	@Override
	public long get(String name) {
		switch(name) {
		case "r0":
		case "r00":
			return Integer.toUnsignedLong(getGPR(0));
		case "r1":
		case "r01":
			return Integer.toUnsignedLong(getGPR(1));
		case "r2":
		case "r02":
			return Integer.toUnsignedLong(getGPR(2));
		case "r3":
		case "r03":
			return Integer.toUnsignedLong(getGPR(3));
		case "r4":
		case "r04":
			return Integer.toUnsignedLong(getGPR(4));
		case "r5":
		case "r05":
			return Integer.toUnsignedLong(getGPR(5));
		case "r6":
		case "r06":
			return Integer.toUnsignedLong(getGPR(6));
		case "r7":
		case "r07":
			return Integer.toUnsignedLong(getGPR(7));
		case "r8":
		case "r08":
			return Integer.toUnsignedLong(getGPR(8));
		case "r9":
		case "r09":
			return Integer.toUnsignedLong(getGPR(9));
		case "r10":
			return Integer.toUnsignedLong(getGPR(10));
		case "r11":
			return Integer.toUnsignedLong(getGPR(11));
		case "r12":
			return Integer.toUnsignedLong(getGPR(12));
		case "r13":
		case "sp":
			return Integer.toUnsignedLong(getGPR(13));
		case "r14":
		case "lr":
			return Integer.toUnsignedLong(getGPR(14));
		case "r15":
		case "pc":
			return Integer.toUnsignedLong(getGPR(15));
		case "cpsr":
			return Integer.toUnsignedLong(getCPSR());
		case "spsr":
			return Integer.toUnsignedLong(getSPSR());
		default:
			throw new IllegalArgumentException("unknown register " + name);
		}
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		for(int i = 0; i < 4; i++) {
			for(int j = 0; j < 4; j++) {
				int r = i * 4 + j;
				buf.append("{{R");
				if(r < 10) {
					buf.append('0');
					buf.append((char) (r + '0'));
				} else {
					buf.append(r);
				}
				buf.append("}}S={{");
				buf.append(HexFormatter.tohex(Integer.toUnsignedLong(getGPR(r)), 8));
				if(j < 3) {
					buf.append("}}x ");
				}
			}
			buf.append("}}x\n");
		}
		int cpsr = getCPSR();
		buf.append("{{CPSR}}S {{");
		buf.append(HexFormatter.tohex(Integer.toUnsignedLong(cpsr), 8));
		buf.append("}}x [");
		buf.append(Cpsr.N.getBit(cpsr) ? 'N' : '-');
		buf.append(Cpsr.Z.getBit(cpsr) ? 'Z' : '-');
		buf.append(Cpsr.C.getBit(cpsr) ? 'C' : '-');
		buf.append(Cpsr.V.getBit(cpsr) ? 'V' : '-');
		buf.append(Cpsr.Q.getBit(cpsr) ? 'Q' : '-');
		buf.append(Cpsr.J.getBit(cpsr) ? 'J' : '-');
		buf.append(Cpsr.E.getBit(cpsr) ? 'E' : '-');
		buf.append(Cpsr.A.getBit(cpsr) ? 'A' : '-');
		buf.append(Cpsr.I.getBit(cpsr) ? 'I' : '-');
		buf.append(Cpsr.F.getBit(cpsr) ? 'F' : '-');
		buf.append(Cpsr.T.getBit(cpsr) ? 'T' : '-');
		buf.append("] Mode: ");
		buf.append(Cpsr.getMode(cpsr));
		buf.append(" / ");
		buf.append(Cpsr.getExecMode(cpsr));
		buf.append('\n');
		return buf.toString();
	}
}
