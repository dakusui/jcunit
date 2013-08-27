package com.github.dakusui.lisj;

import java.io.Serializable;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

public interface Form extends Serializable {
	Object evaluate(Context context, Object params) throws JCUnitException, CUT;

	Object bind(Object... params);

	String name();
}
