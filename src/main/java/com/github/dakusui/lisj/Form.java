package com.github.dakusui.lisj;

import com.github.dakusui.lisj.exceptions.LisjCheckedException;

import java.io.Serializable;

public interface Form extends Serializable {
	Object evaluate(Context context, Object params) throws LisjCheckedException, CUT;

	Object bind(Object... params);

	String name();
}
