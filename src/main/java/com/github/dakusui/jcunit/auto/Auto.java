package com.github.dakusui.jcunit.auto;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

public class Auto extends BaseFunc {

	/**
	 * Serial version UID.
	 */
	private static final long serialVersionUID = -2402014565260025741L;

	@Override
	protected FormResult evaluateLast(
			Context context,
			Object[] evaluatedParams, 
			FormResult lastResult
			)
			throws JCUnitException, CUT {
		FormResult ret = lastResult;
		if (isAlreadyStored()) {
			store();
		} else {
			Object previous = load();
			Object current = null;
			verify(previous, current);
		}
		return ret;
	}

	private void verify(Object previous, Object current) {
		// TODO Auto-generated method stub
	}

	private Object load() {
		// TODO Auto-generated method stub
		return null;
	}

	private void store() {
		// TODO Auto-generated method stub
		
	}

	private boolean isAlreadyStored() {
		// TODO Auto-generated method stub
		return false;
	}


}
