package org.graalvm.vm.trcview.arch.arm.disasm;

import org.graalvm.vm.trcview.disasm.Field;
import org.graalvm.vm.trcview.disasm.Value;

public class InstructionFormat implements Value {
	public final Field cond = field(28, 31);
	public final Field I = field(25);
	public final Field P = field(24);
	public final Field U = field(23);
	public final Field B = field(22);
	public final Field LSH_I = field(22);
	public final Field W = field(21);
	public final Field L = field(20);
	public final Field S = field(20);
	public final Field opcode = field(21, 24);
	public final Field Rn = field(16, 19);
	public final Field Rd = field(12, 15);
	public final Field Rs = field(8, 11);
	public final Field shifter_operand = field(0, 11);
	public final Field addressing_mode_specific = field(0, 11);
	public final Field addr_mode = field(8, 11);
	public final Field LSH_S = field(6);
	public final Field LSH_H = field(5);
	public final Field LSH_addr_mode = field(0, 3);
	public final Field register_list = field(0, 15);
	public final Field immed_24 = field(0, 23);
	public final Field signed_immed_24 = sfield(0, 23);
	public final Field immed1 = field(8, 19);
	public final Field immed2 = field(0, 3);
	public final Field rotate_imm = field(8, 11);
	public final Field immed_8 = field(0, 7);
	public final Field Rm = field(0, 3);

	public final Field H = field(24);

	public final Field decodeBits2726 = field(26, 27);
	public final Field decodeBits2725 = field(25, 27);
	public final Field decodeBits2724 = field(24, 27);
	public final Field decodeBits2720 = field(20, 27);
	public final Field decodeBits2220 = field(20, 22);
	public final Field decodeBits74 = field(4, 7);

	public final Field decodeBits1512 = field(12, 15);

	public static final class DataProcessing extends InstructionFormat {
		public final Field cond = field(28, 31);
		public final Field I = field(25);
		public final Field S = field(20);
		public final Field opcode = field(21, 24);
		public final Field Rn = field(16, 19);
		public final Field Rd = field(12, 15);
		public final Field shifter_operand = field(0, 11);

		// shifter_operand fields
		public final Field rotate_imm = field(8, 11);
		public final Field immed_8 = field(0, 7);

		public final Field shift_imm = field(7, 11);
		public final Field shift = field(5, 6);
		public final Field Rm = field(0, 3);

		public final Field Rs = field(8, 11);
		public final Field bit4 = field(4);

		public DataProcessing() {
			this(0);
		}

		public DataProcessing(int value) {
			super(value);
		}
	}

	public static final class ExceptionGenerating extends InstructionFormat {
		public final Field immed_24 = field(0, 23);
		public final Field immed_h = field(8, 19);
		public final Field immed_l = field(0, 3);

		public ExceptionGenerating() {
			super();
		}

		public ExceptionGenerating(int value) {
			super(value);
		}
	}

	public static final class Coprocessor extends InstructionFormat {
		public final Field opcode = field(4, 7);
		public final Field opcode_1 = field(20, 23);
		public final Field opcode_21 = field(21, 23);
		public final Field P = field(24);
		public final Field U = field(23);
		public final Field N = field(22);
		public final Field W = field(21);
		public final Field CRn = field(16, 19);
		public final Field CRd = field(12, 15);
		public final Field cp_num = field(8, 11);
		public final Field opcode_2 = field(5, 7);
		public final Field CRm = field(0, 3);
		public final Field offset_8 = field(0, 7);
		public final Field option = field(0, 7);

		public Coprocessor() {
			super();
		}

		public Coprocessor(int value) {
			super(value);
		}
	}

	public static final class LoadStoreMultiple extends InstructionFormat {
		public final Field P = field(24);
		public final Field U = field(23);
		public final Field S = field(22);
		public final Field W = field(21);
		public final Field L = field(20);

		public LoadStoreMultiple() {
			super();
		}

		public LoadStoreMultiple(int value) {
			super(value);
		}
	}

	public static final class LoadStore extends InstructionFormat {
		public final Field P = field(24);
		public final Field U = field(23);
		public final Field B = field(22);
		public final Field W = field(21);
		public final Field L = field(20);
		public final Field offset_12 = field(0, 11);
		public final Field shift_imm = field(7, 11);
		public final Field shift = field(5, 6);

		public final Field bit25 = field(25);
		public final Field bit4 = field(4);
		public final Field bit114 = field(4, 11);

		public LoadStore() {
			super();
		}

		public LoadStore(int value) {
			super(value);
		}
	}

	public static final class MiscLoadStore extends InstructionFormat {
		public final Field P = field(24);
		public final Field U = field(23);
		public final Field I = field(22);
		public final Field W = field(21);
		public final Field L = field(20);
		public final Field immedH = field(8, 11);
		public final Field immedL = field(0, 3);
		public final Field H = field(5);
		public final Field S = field(6);

		public MiscLoadStore() {
			super();
		}

		public MiscLoadStore(int value) {
			super(value);
		}
	}

	public static final class MultiplyDivide extends InstructionFormat {
		public final Field Rd = field(16, 19);
		public final Field Rn = field(12, 15);
		public final Field Rs = field(8, 11);
		public final Field Rm = field(0, 3);
		public final Field RdHi = field(16, 19);
		public final Field RdLo = field(12, 15);

		public MultiplyDivide() {
			super();
		}

		public MultiplyDivide(int value) {
			super(value);
		}
	}

	private int value;
	private boolean novalue;

	public InstructionFormat() {
		this.value = 0;
		this.novalue = true;
	}

	public InstructionFormat(int value) {
		this.value = value;
		this.novalue = false;
	}

	protected Field field(int bit) {
		return field(bit, bit);
	}

	protected Field field(int from, int to) {
		return new Field(this, 31 - to, 31 - from);
	}

	protected Field sfield(int from, int to) {
		return new Field(this, 31 - to, 31 - from, true);
	}

	@Override
	public int get() {
		if(novalue) {
			throw new IllegalStateException("no value set");
		}
		return value;
	}

	@Override
	public void set(int value) {
		this.value = value;
		novalue = false;
	}
}
