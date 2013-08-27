package com.github.dakusui.lisj.func.math;

import java.math.BigDecimal;

import com.github.dakusui.lisj.Context;


public class Add extends NumericFunc {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 7188204956641867054L;

	@Override
	protected BigDecimal bigDecimalsEvaluateLast(Context context, BigDecimal[] evaluatedParams) {
		BigDecimal ret = BigDecimal.ZERO;
		for (BigDecimal cur :evaluatedParams) {
			ret = ret.add(cur);
		}
		return ret;
	}

}