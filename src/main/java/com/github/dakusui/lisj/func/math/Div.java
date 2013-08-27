package com.github.dakusui.lisj.func.math;

import java.math.BigDecimal;

import com.github.dakusui.lisj.Context;

import static com.github.dakusui.lisj.Basic.*;


public class Div extends NumericFunc {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 6928768884538561677L;

	@Override
	protected BigDecimal bigDecimalsEvaluateLast(Context context, BigDecimal[] evaluatedParams) {
		BigDecimal ret = null;
		for (BigDecimal cur : evaluatedParams) {
			if (ret == null) ret = cur;
			else ret = ret.divide(cur, context.bigDecimalMathContext());
		}
		return ret;
	}

	@Override
	protected Object checkParams(Object params) {
		super.checkParams(params);
		if (length(params) < 1) {
			throw new IllegalArgumentException();
		}
		return params;
	}
}
