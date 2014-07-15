package com.github.dakusui.lisj.func.str;

import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.LisjUtils;
import com.github.dakusui.lisj.func.BaseFunc;
import org.apache.commons.lang3.ArrayUtils;

import static com.github.dakusui.lisj.Basic.get;
import static com.github.dakusui.lisj.Basic.length;

public class Format extends BaseFunc {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 8667048729923306190L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;
    String format = evaluatedParams[0].toString();
    Object[] args = null;
    if (evaluatedParams.length > 1) {
      args = ArrayUtils.subarray(evaluatedParams, 1, evaluatedParams.length);
    }
    ret.value(String.format(format, args != null ? args : new Object[] { }));
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (length(params) < 1) {
      throw new IllegalArgumentException();
    }
    LisjUtils.checknotnull(get(params, 0));
    return params;
  }
}
