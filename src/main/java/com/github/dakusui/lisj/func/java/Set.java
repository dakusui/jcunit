package com.github.dakusui.lisj.func.java;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

public class Set extends BaseFunc {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -1409212099306827997L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;
    Object obj = Utils.checknotnull(evaluatedParams[0]);
    String attrName = Utils.checknotnull(evaluatedParams[1]).toString();
    Object valueToSet = evaluatedParams[2];
    Utils.setFieldValue(obj, Utils.getField(obj, attrName), valueToSet);
    ret.value(valueToSet);
    return ret;
  }

  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (Basic.length(params) != 3)
      throw new IllegalArgumentException();
    Utils.checknotnull(Basic.get(params, 0));
    Utils.checknotnull(Basic.get(params, 1));
    return params;
  }
}
