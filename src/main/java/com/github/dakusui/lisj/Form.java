package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.Serializable;

public interface Form extends Serializable {
  Object evaluate(Context context, Object params) throws JCUnitException, CUT;

  Object bind(Object... params);

  String name();
}
