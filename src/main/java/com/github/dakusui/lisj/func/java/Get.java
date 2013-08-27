package com.github.dakusui.lisj.func.java;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;


public class Get extends BaseFunc {
	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = 9107185518447270811L;

	@Override
	protected FormResult evaluateLast(Context context, Object[] evaluatedParams, FormResult lastResult) {
		FormResult ret = lastResult;
		Object obj = Utils.checknull(evaluatedParams[0]);
		String attrName = Utils.checknull(evaluatedParams[1]).toString();
		ret.value(Utils.normalize(Utils.getFieldValue(obj, Utils.getField(obj, attrName))));
		return ret;
	}
	
	@Override
	protected Object checkParams(Object params) {
		super.checkParams(params);
		if (Basic.length(params) != 2) throw new IllegalArgumentException();
		Utils.checknull(Basic.get(params, 0));
		Utils.checknull(Basic.get(params, 1));
		return params;
	}
}
