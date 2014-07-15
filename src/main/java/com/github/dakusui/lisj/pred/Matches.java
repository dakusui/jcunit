package com.github.dakusui.lisj.pred;

import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.LisjUtils;

import static com.github.dakusui.lisj.Basic.get;

public class Matches extends BinomialPredicate {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 7402316173263455615L;

	@Override
	protected Object checkParams(Object params) {
		super.checkParams(params);
		LisjUtils.checknotnull(get(params, 0));
		LisjUtils.checknotnull(get(params, 1));
		return params;
	}

	@Override
	protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
	                                  FormResult lastResult) {
		FormResult ret = lastResult;

		String regex = evaluatedParams[1].toString();
		String value = evaluatedParams[0].toString();

		ret.value(value.matches(regex));
		return ret;
	}
}
