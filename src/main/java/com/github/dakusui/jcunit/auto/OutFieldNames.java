package com.github.dakusui.jcunit.auto;

import java.lang.reflect.Field;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

public class OutFieldNames extends BaseFunc {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -7996561462620874712L;

	@Override
	protected FormResult evaluateLast(Context context,
			Object[] evaluatedParams, FormResult lastResult)
			throws JCUnitException, CUT {
		FormResult ret = lastResult;
		Field[] outFields = Utils.getOutFieldsFromClassUnderTest(Basic.get(evaluatedParams, 0).getClass());
		////
		// outFields can never be null since 'checkParams' guarantees it.
		if (outFields.length == 0) {
			ret.value(Basic.NIL);
		} else if (outFields.length == 1) {
			ret.value(Basic.cons(outFields[0].getName(), Basic.NIL));
		} else {
			Object last = null;
			for (Field f : outFields) {
				//// 
				// 'last' can be null only when this path is executed first time
				// in this loop.
				if (last == null) {
					last = f.getName();
				} else {
					ret.value(Basic.cons(last, f.getName()));
					last = ret.value();
				}
			}
		}
		return ret;
	}
	
	@Override
	protected Object checkParams(Object params) {
		super.checkParams(params);
		if (Basic.length(params) != 1) throw new IllegalArgumentException();
		Utils.checknull(Basic.get(params, 0));
		return params;
	}
}
