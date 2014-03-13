package com.github.dakusui.lisj.func;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

@SuppressWarnings("serial")
public abstract class UserFunc extends BaseFunc {
  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitException, CUT {
    lastResult.value(user(evaluatedParams));
    return lastResult;
  }

  protected abstract Object user(Object... evaluatedParams);

  @Override
  public String name() {
    return "#user";
  }
}
