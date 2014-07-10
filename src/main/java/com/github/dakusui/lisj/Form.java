package com.github.dakusui.lisj;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;

import java.io.Serializable;

public interface Form extends Serializable {
  Object evaluate(Context context, Object params) throws JCUnitCheckedException, CUT;

  Object bind(Object... params);

  String name();
}
