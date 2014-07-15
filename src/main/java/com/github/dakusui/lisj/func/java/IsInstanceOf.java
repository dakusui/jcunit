package com.github.dakusui.lisj.func.java;

import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.LisjUtils;
import com.github.dakusui.lisj.func.BaseFunc;

public class IsInstanceOf extends BaseFunc {

  /**
   *
   */
  private static final long serialVersionUID = 5793284792247933434L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws LisjCheckedException, CUT {
    FormResult ret = lastResult;
    Object obj = evaluatedParams[0];
    if (obj != null) {
      Class<?> clazz = LisjUtils.cast(Class.class,
		      LisjUtils.checknotnull(evaluatedParams[1]));
      ret.value(clazz.isAssignableFrom(obj.getClass()));
      return ret;
    }
    ret.value(false);
    return ret;
  }

}
