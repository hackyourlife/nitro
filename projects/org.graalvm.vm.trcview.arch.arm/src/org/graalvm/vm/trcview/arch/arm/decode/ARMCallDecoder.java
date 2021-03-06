package org.graalvm.vm.trcview.arch.arm.decode;

import static org.graalvm.vm.trcview.decode.DecoderUtils.str;

import org.graalvm.vm.trcview.analysis.memory.MemoryNotMappedException;
import org.graalvm.vm.trcview.analysis.type.Function;
import org.graalvm.vm.trcview.analysis.type.Prototype;
import org.graalvm.vm.trcview.analysis.type.Type;
import org.graalvm.vm.trcview.arch.arm.io.ARMCpuState;
import org.graalvm.vm.trcview.arch.io.CpuState;
import org.graalvm.vm.trcview.decode.CallDecoder;
import org.graalvm.vm.trcview.expression.EvaluationException;
import org.graalvm.vm.trcview.expression.ExpressionContext;
import org.graalvm.vm.trcview.net.TraceAnalyzer;

public class ARMCallDecoder extends CallDecoder {
	private static long getArgument(ARMCpuState state, int arg, TraceAnalyzer trc) throws MemoryNotMappedException {
		if(arg <= 3) {
			return state.getGPR(arg);
		} else {
			int sp = state.getGPR(13);
			int pos = sp + (arg - 4) * 4;
			return (int) trc.getI64(pos, state.getStep());
		}
	}

	public static String decode(Function function, ARMCpuState state, ARMCpuState nextState,
			TraceAnalyzer trc) {
		StringBuilder buf = new StringBuilder(function.getName());
		buf.append('(');
		Prototype prototype = function.getPrototype();
		for(int i = 0, arg = 0; i < prototype.args.size(); i++) {
			Type type = prototype.args.get(i);
			String strval = "?";
			if(type.getExpression() != null) {
				try {
					ExpressionContext ctx = new ExpressionContext(state, trc);
					long val = type.getExpression().evaluate(ctx);
					strval = str(type, val, state, trc);
				} catch(EvaluationException e) {
					strval = "?";
				}
			} else {
				try {
					long val = getArgument(state, arg++, trc);
					strval = str(type, val, state, trc);
				} catch(MemoryNotMappedException e) {
					strval = "?";
				}
			}
			if(i > 0) {
				buf.append(", ");
			}
			// val = val & 0xFFFFFFFFL; // truncate to 32bit
			buf.append(strval);
		}
		buf.append(')');
		if(nextState != null) {
			long retval;
			if(prototype.returnType.getExpression() != null) {
				try {
					ExpressionContext ctx = new ExpressionContext(nextState, trc);
					retval = prototype.returnType.getExpression().evaluate(ctx);
				} catch(EvaluationException e) {
					retval = 0;
				}
			} else {
				retval = nextState.getGPR(0);
			}
			retval = retval & 0xFFFFFFFFL; // truncate to 32bit
			String s = str(prototype.returnType, retval, nextState, trc);
			if(s.length() > 0) {
				buf.append(" = ");
				buf.append(s);
			}
		}
		return buf.toString();
	}

	@Override
	public String decode(Function function, CpuState state, CpuState nextState, TraceAnalyzer trc) {
		if(!(state instanceof ARMCpuState) || (nextState != null && !(nextState instanceof ARMCpuState))) {
			return null;
		}
		return decode(function, (ARMCpuState) state, (ARMCpuState) nextState, trc);
	}
}
