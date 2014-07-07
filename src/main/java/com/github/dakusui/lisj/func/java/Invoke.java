package com.github.dakusui.lisj.func.java;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;
import org.apache.commons.lang3.ArrayUtils;

public class Invoke extends BaseFunc {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 714516492393474460L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitException, CUT {
    FormResult ret = lastResult;
    Object obj = Utils.checknotnull(evaluatedParams[0]);
    String methodId = Utils.checknotnull(evaluatedParams[1]).toString();
    Object[] params = ArrayUtils.subarray(evaluatedParams, 2,
        evaluatedParams.length);
    ret.value(Utils.invokeMethod(obj, methodId, params));
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (Basic.length(params) < 2) {
      throw new IllegalArgumentException();
    }
    Utils.checknotnull(Basic.get(params, 0));
    Utils.checknotnull(Basic.get(params, 1));
    return params;
  }
}
