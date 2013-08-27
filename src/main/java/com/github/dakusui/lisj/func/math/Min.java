package com.github.dakusui.lisj.func.math;

import java.math.BigDecimal;

import com.github.dakusui.lisj.Context;

import static com.github.dakusui.lisj.Basic.*;


public class Min extends NumericFunc{
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 2381319246241537526L;

	@Override
	protected BigDecimal bigDecimalsEvaluateLast(Context context, BigDecimal[] evaluatedParams) {
		BigDecimal ret = null;
		for (BigDecimal cur : evaluatedParams) {
			if (ret == null) ret = cur;
			else ret = ret.compareTo(cur) > 0 ? cur : ret;
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
