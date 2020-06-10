package org.graalvm.vm.trcview.arch.arm.disasm;

import org.graalvm.vm.trcview.disasm.Field;

public class Cpsr {
	public static final Field N = Field.getLE(31);
	public static final Field Z = Field.getLE(30);
	public static final Field C = Field.getLE(29);
	public static final Field V = Field.getLE(28);
	public static final Field Q = Field.getLE(27);
	public static final Field J = Field.getLE(24);
	public static final Field E = Field.getLE(9);
	public static final Field A = Field.getLE(8);
	public static final Field I = Field.getLE(7);
	public static final Field F = Field.getLE(6);
	public static final Field T = Field.getLE(5);

	public static final Field GE = Field.getLE(19, 16);
	public static final Field M = Field.getLE(4, 0);

	public static String getMode(int cpsr) {
		int m = M.get(cpsr);
		switch(m) {
		case 0b10000:
			return "User";
		case 0b10001:
			return "FIQ";
		case 0b10010:
			return "IRQ";
		case 0b10011:
			return "Supervisor";
		case 0b10111:
			return "Abort";
		case 0b11011:
			return "Undefined";
		case 0b11111:
			return "System";
		default:
			return "RESERVED";
		}
	}

	public static String getExecMode(int cpsr) {
		if(J.getBit(cpsr)) {
			if(T.getBit(cpsr)) {
				return "RESERVED";
			} else {
				return "Jazelle";
			}
		} else {
			if(T.getBit(cpsr)) {
				return "Thumb";
			} else {
				return "ARM";
			}
		}
	}
}
