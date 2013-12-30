package com.github.dakusui.jcunit.auto;

import java.io.File;
import java.lang.reflect.Field;

import org.junit.rules.TestName;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

public class Auto extends BaseFunc {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -2402014565260025741L;

	/**
	 * This function takes one and only one parameter, which is a name of a field.
	 *  
	 * @see com.github.dakusui.lisj.BaseForm#checkParams(java.lang.Object)
	 */
	@Override
	protected Object checkParams(Object params) {
		super.checkParams(params);
		if (Basic.length(params) != 2) throw new IllegalArgumentException();
		Utils.checknull(Basic.get(params, 0));
		Utils.checknull(Basic.get(params, 1));
		return params;
	}

	@Override
	protected FormResult evaluateLast(
			Context context,
			Object[] evaluatedParams, 
			FormResult lastResult
			)
			throws JCUnitException, CUT {
		FormResult ret = lastResult;
		/*
		 * We can use the first and second elements without a check since 'checkParams'
		 * method guarantees that the array has two and only two elements. 
		 */
		Object obj       = evaluatedParams[0].toString();
		String fieldName = evaluatedParams[1].toString();
		if (isAlreadyStored(obj, fieldName)) {
			store(obj, fieldName);
			ret.value(false);
		} else {
			Object previous = load(obj, fieldName);
			Object current = get(obj, fieldName);
			ret.value(verify(previous, current));
		}
		return ret;
	}

	private File baseDir() {
		return null;
	}

	private File fileForField(File baseDir, TestName name, Field out) {
		return new File(baseDir, this.getClass().getCanonicalName() + "/" + name.getMethodName() + "/" + out.getName());
	}
	
	private Object get(Object obj, String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	private boolean verify(Object previous, Object current) {
		// TODO Auto-generated method stub
		return false;
	}

	private Object load(Object obj, String fieldName) {
		// TODO Auto-generated method stub
		return null;
	}

	private void store(Object obj, String fieldName) {
		// TODO Auto-generated method stub
		
	}

	private boolean isAlreadyStored(Object obj, String fieldName) {
		// TODO Auto-generated method stub
		return false;
	}
}
