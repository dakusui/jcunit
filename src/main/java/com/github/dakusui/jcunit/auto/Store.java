package com.github.dakusui.jcunit.auto;

import org.junit.rules.TestName;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Store extends AutoBase {
	/**
	 * Serial version UID
	 */
	private static final long serialVersionUID = -9189450774385787028L;

	@Override
	protected FormResult evaluateLast(Context context,
			Object[] evaluatedParams, FormResult lastResult)
			throws JCUnitException, CUT {
		FormResult ret = lastResult;
		/*
		 * We can use the first, second, and third elements without a check since 'checkParams'
		 * method guarantees that the array has three and only three elements. 
		 */
		TestName testName = (TestName) evaluatedParams[0]; 
		Object obj        = evaluatedParams[1];
		String fieldName  = evaluatedParams[2].toString();
		store(obj, fieldName, testName);
		ret.value(true);
		return ret;
	}
}
