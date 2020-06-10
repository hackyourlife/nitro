package org.graalvm.vm.trcview.arch.arm.disasm;

import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.Coprocessor;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.DataProcessing;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.ExceptionGenerating;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.LoadStore;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.LoadStoreMultiple;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.MiscLoadStore;
import org.graalvm.vm.trcview.arch.arm.disasm.InstructionFormat.MultiplyDivide;
import org.graalvm.vm.trcview.arch.arm.io.ARMCpuState;
import org.graalvm.vm.trcview.arch.io.InstructionType;
import org.graalvm.vm.util.BitTest;
import org.graalvm.vm.util.HexFormatter;

public class ARMv5Disassembler {
	private static final InstructionFormat insnfmt = new InstructionFormat();

	private static final String[] DATAPROCESSING = {
			"AND", "EOR", "SUB", "RSB",
			"ADD", "ADC", "SBC", "RSC",
			"TST", "TEQ", "CMP", "CMN",
			"ORR", "MOV", "BIC", "MVN"
	};

	public static boolean conditionPassed(int op, int cpsr) {
		int opcd = insnfmt.cond.get(op);
		switch(opcd) {
		case Condition.EQ:
			return Cpsr.Z.getBit(cpsr);
		case Condition.NE:
			return !Cpsr.Z.getBit(cpsr);
		case Condition.CS:
			return Cpsr.C.getBit(cpsr);
		case Condition.CC:
			return !Cpsr.C.getBit(cpsr);
		case Condition.MI:
			return Cpsr.N.getBit(cpsr);
		case Condition.PL:
			return !Cpsr.N.getBit(cpsr);
		case Condition.VS:
			return Cpsr.V.getBit(cpsr);
		case Condition.VC:
			return !Cpsr.V.getBit(cpsr);
		case Condition.HI:
			return Cpsr.C.getBit(cpsr) && !Cpsr.Z.getBit(cpsr);
		case Condition.LS:
			return !Cpsr.C.getBit(cpsr) || Cpsr.Z.getBit(cpsr);
		case Condition.GE:
			return Cpsr.N.getBit(cpsr) == Cpsr.V.getBit(cpsr);
		case Condition.LT:
			return Cpsr.N.getBit(cpsr) != Cpsr.V.getBit(cpsr);
		case Condition.GT:
			return !Cpsr.Z.getBit(cpsr) && Cpsr.N.getBit(cpsr) == Cpsr.V.getBit(cpsr);
		case Condition.LE:
			return Cpsr.Z.getBit(cpsr) || Cpsr.N.getBit(cpsr) != Cpsr.V.getBit(cpsr);
		case Condition.AL:
			return true;
		default:
			return false;
		}
	}

	// @formatter:off
	/*
	 * ARM is weird. The following instructions are a call:
	 * - BL <target>
	 * - MOV LR, PC; MOV PC, <target>
	 * - MOV LR, PC; BX <target>
	 *
	 * The following instructions are a return:
	 * - BX LR
	 * - SUB PC, LR, #4
	 * - SUBS PC, LR, #4
	 * - MOV PC, LR
	 */
	// @formatter:on
	public static InstructionType getType(ARMCpuState state) {
		int op = state.getCode();
		int cpsr = state.getCPSR();
		return getType(op, cpsr);
	}

	public static InstructionType getType(int op, int cpsr) {
		switch(insnfmt.decodeBits2725.get(op)) {
		case 0b100:
			if(!BitTest.test(op, 1 << 22) && BitTest.test(op, 1 << 20)) {
				// LDM (1)
				// TODO: this might be a RET
				if(BitTest.test(insnfmt.register_list.get(op), 1 << 15)) {
					if(conditionPassed(op, cpsr)) {
						return InstructionType.JMP_INDIRECT;
					} else {
						return InstructionType.JCC;
					}
				} else {
					return InstructionType.OTHER;
				}
			} else if(BitTest.test(op, 1 << 22) && BitTest.test(op, 1 << 20) && BitTest.test(op, 1 << 15)) {
				// LDM (1)
				// TODO: this might be a RET
				if(conditionPassed(op, cpsr)) {
					return InstructionType.JMP_INDIRECT;
				} else {
					return InstructionType.JCC;
				}
			}
			break;
		case 0b101:
			if(insnfmt.cond.get(op) == 0b1111) {
				// BLX (1)
				if(conditionPassed(op, cpsr)) {
					return InstructionType.CALL;
				} else {
					return InstructionType.JCC;
				}
			} else {
				// B, BL
				boolean l = BitTest.test(op, 1 << 24);
				if(l && conditionPassed(op, cpsr)) {
					return InstructionType.CALL;
				} else if(insnfmt.cond.get(op) == Condition.AL) {
					return InstructionType.JMP;
				} else {
					return InstructionType.JCC;
				}
			}
		}

		switch(insnfmt.decodeBits2720.get(op)) {
		case 0b00000100:
		case 0b00000101:
		case 0b00100100:
		case 0b00100101:
			// SUB{S}
			if(insnfmt.Rd.get(op) == 15 && insnfmt.Rn.get(op) == 14) {
				// maybe check for insnfmt.I.getBit(op)
				// DataProcessing fmt = new DataProcessing(op);
				// int rotate_imm = fmt.rotate_imm.get();
				// int immed_8 = fmt.immed_8.get();
				// int operand = Integer.rotateRight(immed_8, rotate_imm * 2);
				// if(operand == 4) {
				if(conditionPassed(op, cpsr)) {
					return InstructionType.RTI;
				} else {
					return InstructionType.JCC;
				}
				// }
			} else if(insnfmt.Rd.get(op) == 15) {
				if(conditionPassed(op, cpsr)) {
					return InstructionType.JMP_INDIRECT;
				} else {
					return InstructionType.JCC;
				}
			} else {
				return InstructionType.OTHER;
			}
		case 0b00011010:
		case 0b00011011:
			// MOV{S}
			if(insnfmt.Rd.get(op) == 15) {
				DataProcessing fmt = new DataProcessing(op);
				int shift = fmt.shift.get();
				int shift_imm = fmt.shift_imm.get();
				boolean bit4 = fmt.bit4.getBit();
				if(conditionPassed(op, cpsr)) {
					if(!bit4 && shift == 0 && shift_imm == 0 && fmt.Rm.get() == 14) {
						return InstructionType.RET;
					} else {
						return InstructionType.JMP_INDIRECT;
					}
				} else {
					return InstructionType.JCC;
				}
			} else {
				return InstructionType.OTHER;
			}
		}

		int decodebits = insnfmt.decodeBits74.get(op) | insnfmt.decodeBits2720.get(op) << 4;
		switch(decodebits) {
		case 0b00010010_0001: // BX
		case 0b00010010_0010: // BXJ
			if(conditionPassed(op, cpsr)) {
				if(insnfmt.Rm.get(op) == 14) {
					return InstructionType.RET;
				} else if(insnfmt.cond.get(op) == Condition.AL) {
					return InstructionType.JMP;
				} else {
					return InstructionType.JCC;
				}
			} else if(insnfmt.cond.get(op) == Condition.AL) {
				return InstructionType.JMP;
			} else {
				return InstructionType.JCC;
			}
		case 0b00010010_0011: // BLX (2)
			if(conditionPassed(op, cpsr)) {
				return InstructionType.CALL;
			} else {
				return InstructionType.OTHER;
			}
		}

		return InstructionType.OTHER;
	}

	public static String[] disassemble(int pc, int op) {
		if(op == 0xE1A00000) {
			// MOV R0, R0
			return new String[] { "NOP" };
		}

		if(insnfmt.cond.get(op) == 0b1111) {
			switch(insnfmt.decodeBits1512.get(op)) {
			case 0b1111:
				if(insnfmt.decodeBits2220.get(op) == 0b101 && insnfmt.decodeBits2726.get(op) == 0b01 &&
						BitTest.test(op, 1 << 24)) {
					// PLD
					return combine(new String[] { "PLD" }, loadStore(new LoadStore(op)));
				}
			}
		}
		switch(insnfmt.decodeBits2725.get(op)) {
		case 0b000:
			switch(insnfmt.decodeBits74.get(op)) {
			case 0b1011:
				if(BitTest.test(op, 1 << 20)) {
					// LDRH
					MiscLoadStore fmt = new MiscLoadStore(op);
					return combine(new String[] {
							"LDR" + Condition.getExtension(fmt.cond.get()) + "H",
							r(fmt.Rd.get()) }, miscLoadStore(fmt));
				} else {
					// STRH
					MiscLoadStore fmt = new MiscLoadStore(op);
					return combine(new String[] {
							"STR" + Condition.getExtension(fmt.cond.get()) + "H",
							r(fmt.Rd.get()) }, miscLoadStore(fmt));
				}
			case 0b1101:
				if(!BitTest.test(op, 1 << 20)) {
					// LDRD
					MiscLoadStore fmt = new MiscLoadStore(op);
					return combine(new String[] {
							"LDR" + Condition.getExtension(fmt.cond.get()) + "D",
							r(fmt.Rd.get()) }, miscLoadStore(fmt));
				} else {
					// LDRSB
					MiscLoadStore fmt = new MiscLoadStore(op);
					return combine(new String[] {
							"LDR" + Condition.getExtension(fmt.cond.get()) + "SB",
							r(fmt.Rd.get()) }, miscLoadStore(fmt));
				}
			case 0b1111:
				if(BitTest.test(op, 1 << 20)) {
					// LDRSH
					MiscLoadStore fmt = new MiscLoadStore(op);
					return combine(new String[] {
							"LDR" + Condition.getExtension(fmt.cond.get()) + "SH",
							r(fmt.Rd.get()) }, miscLoadStore(fmt));
				} else {
					// STRD
					MiscLoadStore fmt = new MiscLoadStore(op);
					return combine(new String[] {
							"STR" + Condition.getExtension(fmt.cond.get()) + "D",
							r(fmt.Rd.get()) }, miscLoadStore(fmt));
				}
			}
			break;
		case 0b100:
			if(!BitTest.test(op, 1 << 22) && BitTest.test(op, 1 << 20)) {
				// LDM (1)
				LoadStoreMultiple fmt = new LoadStoreMultiple(op);
				String w = fmt.W.getBit() ? "!" : "";
				return new String[] {
						"LDM" + Condition.getExtension(fmt.cond.get()) + loadStoreMultiple(fmt),
						r(fmt.Rn.get()) + w, Register.list(fmt.register_list.get()) };
			} else if(insnfmt.decodeBits2220.get(op) == 0b101 && !BitTest.test(op, 1 << 15)) {
				// LDM (2)
				LoadStoreMultiple fmt = new LoadStoreMultiple(op);
				return new String[] {
						"LDM" + Condition.getExtension(fmt.cond.get()) + loadStoreMultiple(fmt),
						r(fmt.Rn.get()), Register.list(fmt.register_list.get()) + "^" };
			} else if(BitTest.test(op, 1 << 22) && BitTest.test(op, 1 << 20) && BitTest.test(op, 1 << 15)) {
				// LDM (3)
				LoadStoreMultiple fmt = new LoadStoreMultiple(op);
				String w = fmt.W.getBit() ? "!" : "";
				return new String[] {
						"LDM" + Condition.getExtension(fmt.cond.get()) + loadStoreMultiple(fmt),
						r(fmt.Rn.get()) + w, Register.list(fmt.register_list.get()) + "^" };
			} else if(!BitTest.test(op, 1 << 22) && !BitTest.test(op, 1 << 20)) {
				// STM (1)
				LoadStoreMultiple fmt = new LoadStoreMultiple(op);
				String w = fmt.W.getBit() ? "!" : "";
				return new String[] {
						"STM" + Condition.getExtension(fmt.cond.get()) + loadStoreMultiple(fmt),
						r(fmt.Rn.get()) + w, Register.list(fmt.register_list.get()) };
			} else if(insnfmt.decodeBits2220.get(op) == 0b100) {
				// STM (2)
				LoadStoreMultiple fmt = new LoadStoreMultiple(op);
				return new String[] {
						"STM" + Condition.getExtension(fmt.cond.get()) + loadStoreMultiple(fmt),
						r(fmt.Rn.get()), Register.list(fmt.register_list.get()) + "^" };
			}
			break;
		case 0b101:
			if(insnfmt.cond.get(op) == 0b1111) {
				// BLX (1)
				boolean h = insnfmt.H.getBit(op);
				int imm = insnfmt.signed_immed_24.get(op);
				int off = imm << 2 + (h ? 2 : 0);
				int dst = pc(pc + off);
				return new String[] { "BLX",
						"0x" + HexFormatter.tohex(Integer.toUnsignedLong(dst)).toUpperCase() };
			} else {
				// B, BL
				boolean l = BitTest.test(op, 1 << 24);
				int imm = insnfmt.signed_immed_24.get(op);
				int off = imm << 2;
				int dst = pc(pc + off);
				return new String[] {
						"B" + (l ? "L" : "") + Condition.getExtension(insnfmt.cond.get(op)),
						"0x" + HexFormatter.tohex(Integer.toUnsignedLong(dst)).toUpperCase() };
			}
		case 0b110:
			if(BitTest.test(op, 1 << 20)) {
				// LDC, LDC2
				Coprocessor cp = new Coprocessor(op);
				boolean l = cp.N.getBit();
				int cond = cp.cond.get();
				if(cond == 0b1111) {
					return combine(new String[] { "LDC2" + (l ? "L" : ""), cp(cp.cp_num.get()),
							cr(cp.CRd.get()) }, coprocessorOperand(cp));
				} else {
					return combine(new String[] {
							"LDC" + Condition.getExtension(cond) + (l ? "L" : ""),
							cp(cp.cp_num.get()), cr(cp.CRd.get()) },
							coprocessorOperand(cp));
				}
			} else {
				// STC, STC2
				Coprocessor cp = new Coprocessor(op);
				boolean l = cp.N.getBit();
				int cond = cp.cond.get();
				if(cond == 0b1111) {
					return combine(new String[] { "STC2" + (l ? "L" : ""), cp(cp.cp_num.get()),
							cr(cp.CRd.get()) }, coprocessorOperand(cp));
				} else {
					return combine(new String[] {
							"STC" + Condition.getExtension(cond) + (l ? "L" : ""),
							cp(cp.cp_num.get()), cr(cp.CRd.get()) },
							coprocessorOperand(cp));
				}
			}
		}

		switch(insnfmt.decodeBits2724.get(op)) {
		case 0b1110:
			if(BitTest.test(op, 1 << 4)) {
				Coprocessor cp = new Coprocessor(op);
				if(!BitTest.test(op, 1 << 20)) {
					// MCR, MCR2
					if(cp.cond.get() == 0b1111) {
						if(cp.opcode_2.get() != 0) {
							return new String[] { "MCR2", cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_21.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()),
									Integer.toString(cp.opcode_2.get()) };
						} else {
							return new String[] { "MCR2", cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_21.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()) };
						}
					} else {
						if(cp.opcode_2.get() != 0) {
							return new String[] {
									"MCR" + Condition.getExtension(cp.cond.get()),
									cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_21.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()),
									Integer.toString(cp.opcode_2.get()) };
						} else {
							return new String[] {
									"MCR" + Condition.getExtension(cp.cond.get()),
									cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_21.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()) };
						}
					}
				} else {
					// MRC, MRC2
					if(cp.cond.get() == 0b1111) {
						if(cp.opcode_2.get() != 0) {
							return new String[] { "MRC2", cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_1.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()),
									Integer.toString(cp.opcode_2.get()) };
						} else {
							return new String[] { "MRC2", cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_1.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()) };
						}
					} else {
						if(cp.opcode_2.get() != 0) {
							return new String[] {
									"MRC" + Condition.getExtension(cp.cond.get()),
									cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_1.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()),
									Integer.toString(cp.opcode_2.get()) };
						} else {
							return new String[] {
									"MRC" + Condition.getExtension(cp.cond.get()),
									cp(cp.cp_num.get()),
									Integer.toString(cp.opcode_1.get()),
									r(cp.Rd.get()), cr(cp.CRn.get()),
									cr(cp.CRm.get()) };
						}
					}
				}
			} else {
				// CDP, CDP2
				Coprocessor cp = new Coprocessor(op);
				if(cp.cond.get() == 0b1111) {
					return new String[] { "CDP2", cp(cp.cp_num.get()),
							Integer.toString(cp.opcode_1.get()), cr(cp.CRd.get()),
							cr(cp.CRn.get()), cr(cp.CRm.get()),
							Integer.toString(cp.opcode_2.get()) };
				} else {
					return new String[] { "CDP" + Condition.getExtension(cp.cond.get()),
							cp(cp.cp_num.get()),
							Integer.toString(cp.opcode_1.get()), cr(cp.CRd.get()),
							cr(cp.CRn.get()), cr(cp.CRm.get()),
							Integer.toString(cp.opcode_2.get()) };
				}
			}
		}

		ExceptionGenerating exc = new ExceptionGenerating(op);
		if(exc.decodeBits2724.get() == 0b1111) {
			int imm = exc.immed_24.get();
			return new String[] { "SWI" + Condition.getExtension(exc.cond.get()),
					"#" + imm };
		} else if(exc.cond.get() == 0b1110 && exc.decodeBits2720.get() == 0b00010010 &&
				exc.decodeBits74.get() == 0b0111) {
			int imm = exc.immed_h.get() << 4 | exc.immed_l.get();
			return new String[] { "BKPT", "#" + imm };
		}

		int decodebits = insnfmt.decodeBits74.get(op) | insnfmt.decodeBits2720.get(op) << 4;
		switch(decodebits) {
		case 0b00010010_0001: // BX
			return new String[] { "BX" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rm.get(op)) };
		case 0b00010010_0010: // BXJ
			return new String[] { "BXJ" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rm.get(op)) };
		case 0b00010010_0011: // BLX (2)
			return new String[] { "BLX" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rm.get(op)) };
		case 0b00010110_0001: // CLZ
			return new String[] { "CLZ" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), r(insnfmt.Rm.get(op)) };
		case 0b00000010_1001: { // MLA
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "MLA" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get(op)),
					r(fmt.Rm.get(op)), r(fmt.Rs.get()), r(fmt.Rn.get(op)) };
		}
		case 0b00000011_1001: { // MLAS
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "MLA" + Condition.getExtension(insnfmt.cond.get(op)) + "S",
					r(fmt.Rd.get(op)), r(fmt.Rm.get(op)), r(fmt.Rs.get()), r(fmt.Rn.get(op)) };
		}
		case 0b00010000_0101: // QADD
			return new String[] { "QADD" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), r(insnfmt.Rm.get(op)), r(insnfmt.Rn.get(op)) };
		case 0b00010100_0101: // QDADD
			return new String[] { "QDADD" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), r(insnfmt.Rm.get(op)), r(insnfmt.Rn.get(op)) };
		case 0b00010110_0101: // QDSUB
			return new String[] { "QDSUB" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), r(insnfmt.Rm.get(op)), r(insnfmt.Rn.get(op)) };
		case 0b00010010_0101: // QSUB
			return new String[] { "QSUB" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), r(insnfmt.Rm.get(op)), r(insnfmt.Rn.get(op)) };
		case 0b00010000_1000: {
			// SMLABB
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLABB" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()), r(fmt.Rn.get()) };
		}
		case 0b00010000_1010: {
			// SMLATB
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLATB" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()), r(fmt.Rn.get()) };
		}
		case 0b00010000_1100: {
			// SMLABT
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLABT" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()), r(fmt.Rn.get()) };
		}
		case 0b00010000_1110: {
			// SMLATT
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLATT" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()), r(fmt.Rn.get()) };
		}
		case 0b00001110_1001: {
			// SMLAL
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLAL" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00001111_1001: {
			// SMLALS
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLAL" + Condition.getExtension(fmt.cond.get()) + "S", r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010100_1000: {
			// SMLALBB
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLALBB" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010100_1010: {
			// SMLALTB
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLALTB" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010100_1100: {
			// SMLALBT
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLALBT" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010100_1110: {
			// SMLALTT
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLALTT" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010010_1000: {
			// SMLAWB
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLAWB" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()), r(fmt.Rn.get()) };
		}
		case 0b00010010_1100: {
			// SMLAWT
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMLAWT" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()), r(fmt.Rn.get()) };
		}
		case 0b00010110_1000: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULBB" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010110_1010: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULTB" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010110_1100: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULBT" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010110_1110: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULTT" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00001100_1001: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULL" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00001101_1001: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULL" + Condition.getExtension(fmt.cond.get()) + "S", r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010010_1010: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULWB" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010010_1110: {
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "SMULWT" + Condition.getExtension(fmt.cond.get()), r(fmt.Rd.get()),
					r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00010000_1001:
			// SWP
			return new String[] { "SWP" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), r(insnfmt.Rm.get()), "[" + r(insnfmt.Rn.get()) + "]" };
		case 0b00010100_1001:
			// SWPB
			return new String[] { "SWP" + Condition.getExtension(insnfmt.cond.get(op)) + "B",
					r(insnfmt.Rd.get(op)), r(insnfmt.Rm.get()), "[" + r(insnfmt.Rn.get()) + "]" };
		case 0b00001010_1001: {
			// UMLAL
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "UMLAL" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00001011_1001: {
			// UMLALS
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "UMLAL" + Condition.getExtension(fmt.cond.get()) + "S", r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00001000_1001: {
			// UMULL
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "UMULL" + Condition.getExtension(fmt.cond.get()), r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		case 0b00001001_1001: {
			// UMULLS
			MultiplyDivide fmt = new MultiplyDivide(op);
			return new String[] { "UMULL" + Condition.getExtension(fmt.cond.get()) + "S", r(fmt.RdLo.get()),
					r(fmt.RdHi.get()), r(fmt.Rm.get()), r(fmt.Rs.get()) };
		}
		}

		switch(insnfmt.decodeBits2720.get(op)) {
		case 0b00110010: { // MSR CPSR_<fields>, <imm>
			int rotate_imm = insnfmt.rotate_imm.get();
			int immed_8 = insnfmt.immed_8.get();
			int operand = Integer.rotateRight(immed_8, rotate_imm * 2);
			StringBuilder fields = new StringBuilder();
			if(BitTest.test(op, 1 << 19)) {
				fields.append('f');
			}
			if(BitTest.test(op, 1 << 18)) {
				fields.append('s');
			}
			if(BitTest.test(op, 1 << 17)) {
				fields.append('x');
			}
			if(BitTest.test(op, 1 << 16)) {
				fields.append('c');
			}
			return new String[] { "MSR" + Condition.getExtension(insnfmt.cond.get(op)), "CPSR_" + fields,
					imm(operand) };
		}
		case 0b00110110: { // MSR SPSR_<fields>, <imm>
			int rotate_imm = insnfmt.rotate_imm.get();
			int immed_8 = insnfmt.immed_8.get();
			int operand = Integer.rotateRight(immed_8, rotate_imm * 2);
			StringBuilder fields = new StringBuilder();
			if(BitTest.test(op, 1 << 19)) {
				fields.append('f');
			}
			if(BitTest.test(op, 1 << 18)) {
				fields.append('s');
			}
			if(BitTest.test(op, 1 << 17)) {
				fields.append('x');
			}
			if(BitTest.test(op, 1 << 16)) {
				fields.append('c');
			}
			return new String[] { "MSR" + Condition.getExtension(insnfmt.cond.get(op)), "SPSR_" + fields,
					imm(operand) };
		}
		case 0b00010010: { // MSR CPSR_<fields>, Rm
			StringBuilder fields = new StringBuilder();
			if(BitTest.test(op, 1 << 19)) {
				fields.append('f');
			}
			if(BitTest.test(op, 1 << 18)) {
				fields.append('s');
			}
			if(BitTest.test(op, 1 << 17)) {
				fields.append('x');
			}
			if(BitTest.test(op, 1 << 16)) {
				fields.append('c');
			}
			return new String[] { "MSR" + Condition.getExtension(insnfmt.cond.get(op)), "CPSR_" + fields,
					r(insnfmt.Rm.get(op)) };
		}
		case 0b00010110: { // MSR SPSR_<fields>, Rm
			StringBuilder fields = new StringBuilder();
			if(BitTest.test(op, 1 << 19)) {
				fields.append('f');
			}
			if(BitTest.test(op, 1 << 18)) {
				fields.append('s');
			}
			if(BitTest.test(op, 1 << 17)) {
				fields.append('x');
			}
			if(BitTest.test(op, 1 << 16)) {
				fields.append('c');
			}
			return new String[] { "MSR" + Condition.getExtension(insnfmt.cond.get(op)), "SPSR_" + fields,
					r(insnfmt.Rm.get(op)) };
		}
		case 0b00010000: // MRS Rd, CPSR
			return new String[] { "MRS" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), "CPSR" };
		case 0b00010100: // MRS Rd, SPSR
			return new String[] { "MRS" + Condition.getExtension(insnfmt.cond.get(op)),
					r(insnfmt.Rd.get(op)), "SPSR" };
		case 0b11000100:
			// MCRR, MCRR2
			if(insnfmt.cond.get() == 0b1111) {
				Coprocessor cp = new Coprocessor(op);
				return new String[] { "MCRR2", cp(cp.cp_num.get()), Integer.toString(cp.opcode.get()),
						r(cp.Rd.get()), r(cp.Rn.get()), cr(cp.CRm.get()) };
			} else {
				Coprocessor cp = new Coprocessor(op);
				return new String[] { "MCRR" + Condition.getExtension(cp.cond.get()),
						cp(cp.cp_num.get()), Integer.toString(cp.opcode.get()), r(cp.Rd.get()),
						r(cp.Rn.get()), cr(cp.CRm.get()) };
			}
		case 0b11000101:
			// MRRC, MRRC2
			if(insnfmt.cond.get() == 0b1111) {
				Coprocessor cp = new Coprocessor(op);
				return new String[] { "MRRC2", cp(cp.cp_num.get()), Integer.toString(cp.opcode.get()),
						r(cp.Rd.get()), r(cp.Rn.get()), cr(cp.CRm.get()) };
			} else {
				Coprocessor cp = new Coprocessor(op);
				return new String[] { "MRRC" + Condition.getExtension(cp.cond.get()),
						cp(cp.cp_num.get()), Integer.toString(cp.opcode.get()), r(cp.Rd.get()),
						r(cp.Rn.get()), cr(cp.CRm.get()) };
			}
		case 0b00000000:
		case 0b00000001:
			switch(insnfmt.decodeBits74.get(op)) {
			case 0b1001:
				// MUL{S}
				return new String[] {
						"MUL" + Condition.getExtension(insnfmt.cond.get(op)) +
								(insnfmt.S.getBit() ? "S" : ""),
						r(insnfmt.Rd.get()), r(insnfmt.Rs.get()), r(insnfmt.Rm.get()) };
			}
			break;
		}

		if(insnfmt.decodeBits2726.get(op) == 0) {
			DataProcessing fmt = new DataProcessing(op);
			int opcd = fmt.opcode.get();
			String opcode = DATAPROCESSING[opcd];
			String s = fmt.S.getBit() ? "S" : "";
			String cond = Condition.getExtension(fmt.cond.get());
			String[] shifterOperand = shifterOperandData(fmt);
			if(opcd == 0b1111 || opcd == 0b1101) {
				// MOV | MVN
				if(opcd == 0b1111 && fmt.I.getBit()) {
					// substitute MVN by MOV if possible
					int rotate_imm = fmt.rotate_imm.get();
					int immed_8 = fmt.immed_8.get();
					int operand = Integer.rotateRight(immed_8, rotate_imm * 2);
					return new String[] { "MOV" + s + cond, r(fmt.Rd.get()), imm(~operand) };
				} else {
					return combine(new String[] { opcode + s + cond, r(fmt.Rd.get()) },
							shifterOperand);
				}
			} else if(opcd == 0b1010 || opcd == 0b1011 || opcd == 0b1000 || opcd == 0b1011) {
				// CMP | CMN | TST | TEQ
				return combine(new String[] { opcode + cond, r(fmt.Rn.get()) }, shifterOperand);
			} else {
				return combine(new String[] { opcode + s + cond, r(fmt.Rd.get()), r(fmt.Rn.get()) },
						shifterOperand);
			}
		} else if(insnfmt.decodeBits2726.get(op) == 1) {
			if(!BitTest.test(op, 1 << 24) && insnfmt.decodeBits2220.get(op) == 0b111) {
				// LDRBT
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "LDRBT" + Condition.getExtension(fmt.cond.get()) + "BT",
						r(fmt.Rd.get()) }, loadStore(fmt));
			} else if(!BitTest.test(op, 1 << 22) && BitTest.test(op, 1 << 20)) {
				// LDR
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "LDR" + Condition.getExtension(fmt.cond.get()),
						r(fmt.Rd.get()) }, loadStore(fmt));
			} else if(BitTest.test(op, 1 << 22) && BitTest.test(op, 1 << 20)) {
				// LDRB
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "LDR" + Condition.getExtension(fmt.cond.get()) + "B",
						r(fmt.Rd.get()) }, loadStore(fmt));
			} else if(!BitTest.test(op, 1 << 24) && insnfmt.decodeBits2220.get(op) == 0b011) {
				// LDRT
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "LDR" + Condition.getExtension(fmt.cond.get()) + "T",
						r(fmt.Rd.get()) }, loadStore(fmt));
			} else if(!BitTest.test(op, 1 << 22) && !BitTest.test(op, 1 << 20)) {
				// STR
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "STR" + Condition.getExtension(fmt.cond.get()),
						r(fmt.Rd.get()) }, loadStore(fmt));
			} else if(BitTest.test(op, 1 << 22) && !BitTest.test(op, 1 << 20)) {
				// STRB
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "STR" + Condition.getExtension(fmt.cond.get()) + "B",
						r(fmt.Rd.get()) }, loadStore(fmt));
			} else if(!BitTest.test(op, 1 << 24) && insnfmt.decodeBits2220.get(op) == 0b110) {
				// STRBT
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "STR" + Condition.getExtension(fmt.cond.get()) + "BT",
						r(fmt.Rd.get()) }, loadStore(fmt));
			} else if(!BitTest.test(op, 1 << 24) && insnfmt.decodeBits2220.get(op) == 0b010) {
				// STRT
				LoadStore fmt = new LoadStore(op);
				return combine(new String[] { "STR" + Condition.getExtension(fmt.cond.get()) + "T",
						r(fmt.Rd.get()) }, loadStore(fmt));
			}
		}

		return new String[] { "; unknown" };
	}

	private static int pc(int pc) {
		return pc;
	}

	public static String r(int r) {
		return Register.getR(r);
	}

	public static String cr(int r) {
		return "c" + r;
	}

	public static String cp(int cp_num) {
		return "p" + cp_num;
	}

	public static String[] combine(String[] a, String[] b) {
		String[] result = new String[a.length + b.length];
		System.arraycopy(a, 0, result, 0, a.length);
		System.arraycopy(b, 0, result, a.length, b.length);
		return result;
	}

	public static String imm(int operand) {
		long op = Integer.toUnsignedLong(operand);
		if(op < 10) {
			return "#" + operand;
		} else {
			return "#0x" + HexFormatter.tohex(op).toUpperCase();
		}
	}

	public static String simm(int operand) {
		long op = operand;
		if(op < 0) {
			op = -op;
		}

		String sign = operand < 0 ? "-" : "";
		if(op < 10) {
			return "#" + sign + operand;
		} else {
			return "#" + sign + "0x" + HexFormatter.tohex(op).toUpperCase();
		}
	}

	public static String[] shifterOperandData(DataProcessing fmt) {
		boolean i = fmt.I.getBit();
		if(i) {
			int rotate_imm = fmt.rotate_imm.get();
			int immed_8 = fmt.immed_8.get();
			int operand = Integer.rotateRight(immed_8, rotate_imm * 2);
			return new String[] { imm(operand) };
		} else {
			int shift = fmt.shift.get();
			int shift_imm = fmt.shift_imm.get();
			boolean bit4 = fmt.bit4.getBit();
			if(!bit4) {
				if(shift == 0 && shift_imm == 0) {
					return new String[] { r(fmt.Rm.get()) };
				} else if(shift == 0) {
					return new String[] { r(fmt.Rm.get()), "LSL #" + shift_imm };
				} else if(shift == 1) {
					return new String[] { r(fmt.Rm.get()), "LSR #" + shift_imm };
				} else if(shift == 2) {
					return new String[] { r(fmt.Rm.get()), "ASR #" + shift_imm };
				} else if(shift == 3) {
					if(shift_imm == 0) {
						return new String[] { r(fmt.Rm.get()), "RRX" };
					} else {
						return new String[] { r(fmt.Rm.get()), "ROR #" + shift_imm };
					}
				}
			} else {
				if(shift == 0) {
					return new String[] { r(fmt.Rm.get()), "LSL " + r(fmt.Rs.get()) };
				} else if(shift == 1) {
					return new String[] { r(fmt.Rm.get()), "LSR " + r(fmt.Rs.get()) };
				} else if(shift == 2) {
					return new String[] { r(fmt.Rm.get()), "ASR " + r(fmt.Rs.get()) };
				} else if(shift == 2) {
					return new String[] { r(fmt.Rm.get()), "ROR " + r(fmt.Rs.get()) };
				}
			}
		}
		return new String[] { "???" };
	}

	public static String[] coprocessorOperand(Coprocessor fmt) {
		// TODO
		boolean p = fmt.P.getBit();
		boolean w = fmt.W.getBit();
		boolean u = fmt.U.getBit();
		if(p) {
			int off = (u ? 1 : -1) * fmt.offset_8.get() * 4;
			String offset = simm(off);
			if(!w) {
				// immediate offset
				return new String[] { "[" + r(fmt.Rn.get()) + ", #" + offset + "]" };
			} else {
				// immediate pre-indexed
				return new String[] { "[" + r(fmt.Rn.get()) + ", #" + offset + "]!" };
			}
		} else if(w) {
			// immediate post-indexed
			int off = (u ? 1 : -1) * fmt.offset_8.get() * 4;
			String offset = simm(off);
			return new String[] { "[" + r(fmt.Rn.get()) + "]", "#" + offset };
		} else {
			int option = fmt.option.get();
			if(option != 0) {
				return new String[] { "[" + r(fmt.Rn.get()) + "]" };
			} else {
				return new String[] { "[" + r(fmt.Rn.get()) + "]", "{" + option + "}" };
			}
		}
	}

	public static String loadStoreMultiple(LoadStoreMultiple fmt) {
		return loadStoreMultiple(fmt, true);
	}

	public static String loadStoreMultiple(LoadStoreMultiple fmt, boolean stack) {
		boolean l = fmt.L.getBit();
		boolean p = fmt.P.getBit();
		boolean u = fmt.U.getBit();
		if(stack) {
			int bits = (l ? 4 : 0) | (p ? 2 : 0) | (u ? 1 : 0);
			switch(bits) {
			case 0b100:
			case 0b011:
				return "FA";
			case 0b101:
			case 0b010:
				return "FD";
			case 0b110:
			case 0b001:
				return "EA";
			case 0b111:
			case 0b000:
				return "ED";
			default:
				return "??";
			}
		} else {
			int bits = (l ? 4 : 0) | (p ? 2 : 0) | (u ? 1 : 0);
			switch(bits) {
			case 0b00:
				return "IA";
			case 0b01:
				return "IA";
			case 0b10:
				return "DB";
			case 0b11:
				return "IB";
			default:
				return "??";
			}
		}
	}

	public static String[] loadStore(LoadStore fmt) {
		boolean p = fmt.P.getBit();
		boolean w = fmt.W.getBit();
		boolean u = fmt.U.getBit();
		boolean bit25 = fmt.bit25.getBit();
		int bit114 = fmt.bit114.get();
		boolean bit4 = fmt.bit4.getBit();
		if(!bit25 && p && !w) {
			// immediate offset
			String off = simm((u ? 1 : -1) * fmt.offset_12.get());
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + off + "]" };
		} else if(bit25 && p && !w && bit114 == 0) {
			// register offset
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + (u ? "+" : "-") + r(fmt.Rm.get()) + "]" };
		} else if(bit25 && p && !w && !bit4) {
			// scaled register offset
			int imm = fmt.shift.get();
			String last;
			switch(imm) {
			default:
			case 0b00:
				last = "LSL " + imm(fmt.shift_imm.get());
				break;
			case 0b01:
				last = "LSR " + imm(fmt.shift_imm.get());
				break;
			case 0b10:
				last = "ASR " + imm(fmt.shift_imm.get());
				break;
			case 0b11:
				if(fmt.shift_imm.get() == 0) {
					last = "RRX";
				} else {
					last = "ROR " + imm(fmt.shift_imm.get());
				}
			}
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + (u ? "+" : "-") + r(fmt.Rm.get()) + ", " +
					last + "]" };
		} else if(!bit25 && p && w) {
			// immediate pre-indexed
			String off = simm((u ? 1 : -1) * fmt.offset_12.get());
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + off + "]!" };
		} else if(bit25 && p && w && bit114 == 0) {
			// register pre-index
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + (u ? "+" : "-") + r(fmt.Rm.get()) + "]!" };
		} else if(bit25 && p && w && !bit4) {
			// scaled register offset
			int imm = fmt.shift.get();
			String last;
			switch(imm) {
			default:
			case 0b00:
				last = "LSL " + imm(fmt.shift_imm.get());
				break;
			case 0b01:
				last = "LSR " + imm(fmt.shift_imm.get());
				break;
			case 0b10:
				last = "ASR " + imm(fmt.shift_imm.get());
				break;
			case 0b11:
				if(fmt.shift_imm.get() == 0) {
					last = "RRX";
				} else {
					last = "ROR " + imm(fmt.shift_imm.get());
				}
			}
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + (u ? "+" : "-") + r(fmt.Rm.get()) + ", " +
					last + "]!" };
		} else if(!bit25 && !p && !w) {
			// immediate post-indexed
			String off = simm((u ? 1 : -1) * fmt.offset_12.get());
			return new String[] { "[" + r(fmt.Rn.get()) + "]", off };
		} else if(bit25 && !p && !w && bit114 == 0) {
			// register post-indexed
			return new String[] { "[" + r(fmt.Rn.get()) + "]", (u ? "+" : "-") + r(fmt.Rm.get()) };
		} else if(bit25 && !p && !w && !bit4) {
			// scaled register offset
			int imm = fmt.shift.get();
			String last;
			switch(imm) {
			default:
			case 0b00:
				last = "LSL " + imm(fmt.shift_imm.get());
				break;
			case 0b01:
				last = "LSR " + imm(fmt.shift_imm.get());
				break;
			case 0b10:
				last = "ASR " + imm(fmt.shift_imm.get());
				break;
			case 0b11:
				if(fmt.shift_imm.get() == 0) {
					last = "RRX";
				} else {
					last = "ROR " + imm(fmt.shift_imm.get());
				}
			}
			return new String[] { "[" + r(fmt.Rn.get()) + "]", (u ? "+" : "-") + r(fmt.Rm.get()), last };
		}
		return new String[] { "; invalid addressing mode" };
	}

	public static String[] miscLoadStore(MiscLoadStore fmt) {
		boolean p = fmt.P.getBit();
		boolean u = fmt.U.getBit();
		boolean w = fmt.W.getBit();
		boolean i = fmt.I.getBit();
		if(p && i && !w) {
			// immediate offset
			int offset_8 = (fmt.immedH.get() << 4) | fmt.immedL.get();
			if(offset_8 == 0) {
				return new String[] { "[" + r(fmt.Rn.get()) + "]" };
			} else {
				String off = simm((u ? 1 : -1) * offset_8);
				return new String[] { "[" + r(fmt.Rn.get()) + ", " + off + "]" };
			}
		} else if(p && !i && !w) {
			// register offset
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + (u ? "+" : "-") + r(fmt.Rm.get()) + "]" };
		} else if(p && i && w) {
			// immediate pre-indexed
			int offset_8 = (fmt.immedH.get() << 4) | fmt.immedL.get();
			String off = simm((u ? 1 : -1) * offset_8);
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + off + "]!" };
		} else if(p && !i && w) {
			// register pre-indexed
			return new String[] { "[" + r(fmt.Rn.get()) + ", " + (u ? "+" : "-") + r(fmt.Rm.get()) + "]!" };
		} else if(!p && i && !w) {
			// immediate post-indexed
			int offset_8 = (fmt.immedH.get() << 4) | fmt.immedL.get();
			String off = simm((u ? 1 : -1) * offset_8);
			return new String[] { "[" + r(fmt.Rn.get()) + "]", off };
		} else if(!p && !i && w) {
			// register post-indexed
			return new String[] { "[" + r(fmt.Rn.get()) + "]", (u ? "+" : "-") + r(fmt.Rm.get()) };
		}
		return new String[] { "; invalid addressing mode" };
	}
}
