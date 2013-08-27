package com.github.dakusui.lisj.func.str;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;

import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

import static com.github.dakusui.lisj.Basic.*;

public class Concat extends BaseFunc {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -2665259624177150641L;

	@Override
	protected FormResult evaluateLast(Context context,
			Object[] evaluatedParams, FormResult lastResult) {
		FormResult ret = lastResult;
		if (evaluatedParams.length == 0 || evaluatedParams.length == 1) {
			lastResult.value("");
			return ret;
		}
		String sep = evaluatedParams[0] == null ? "" : evaluatedParams[0].toString();
		ret.value(StringUtils.join(ArrayUtils.subarray(evaluatedParams, 1, evaluatedParams.length), sep));
		return ret;
	}
	
	@Override
	protected Object checkParams(Object params) {
		super.checkParams(params);
		if (length(params) < 1) { throw new IllegalArgumentException(); }
		return params;
	}
}
