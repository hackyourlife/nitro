package org.graalvm.vm.trcview.arch.arm.disasm;

public class Condition {
	public static final int EQ = 0b0000;
	public static final int NE = 0b0001;
	public static final int CS = 0b0010;
	public static final int HS = 0b0010;
	public static final int CC = 0b0011;
	public static final int LO = 0b0011;
	public static final int MI = 0b0100;
	public static final int PL = 0b0101;
	public static final int VS = 0b0110;
	public static final int VC = 0b0111;
	public static final int HI = 0b1000;
	public static final int LS = 0b1001;
	public static final int GE = 0b1010;
	public static final int LT = 0b1011;
	public static final int GT = 0b1100;
	public static final int LE = 0b1101;
	public static final int AL = 0b1110;

	public static final String[] EXTENSIONS = { "EQ", "NE", "CS", "CC", "MI", "PL", "VS", "VC", "HI", "LS", "GE",
			"LT", "GT", "LE", "", "" };

	public static String getExtension(int cond) {
		return EXTENSIONS[cond & 0x0F];
	}
}
