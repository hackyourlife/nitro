package org.graalvm.vm.trcview.arch.arm.disasm;

import org.graalvm.vm.util.BitTest;

public class Register {
	public static String getR(int r) {
		if(r == 15) {
			return "PC";
		} else if(r == 14) {
			return "LR";
		} else if(r == 13) {
			return "SP";
		} else if(r < 13 && r >= 0) {
			return "R" + r;
		} else {
			throw new IllegalArgumentException("invalid register " + r);
		}
	}

	public static String list(int register_list) {
		StringBuilder buf = new StringBuilder();
		buf.append('{');

		boolean first = true;
		int start = -1;

		for(int i = 0; i < 16; i++) {
			int bit = 1 << i;
			if(BitTest.test(register_list, bit)) {
				// current register is set
				if(i >= 13) {
					// special handling for SP/LR/PC
					if(start != -1) {
						if(start < i - 1) {
							buf.append('-');
							buf.append(getR(i - 1));
						}
						start = -1;
					}
					if(!first) {
						buf.append(',');
					} else {
						first = false;
					}
					buf.append(getR(i));
				} else {
					if(start == -1) {
						// start new range
						if(!first) {
							buf.append(',');
						} else {
							first = false;
						}
						buf.append(getR(i));
						start = i;
					} else {
						// nothing
					}
				}
			} else if(start != -1) {
				if(start < i - 1) {
					buf.append('-');
					buf.append(getR(i - 1));
				}
				start = -1;
			}
		}

		assert start == -1;
		assert buf.length() > 1;

		return buf.append('}').toString();
	}
}
