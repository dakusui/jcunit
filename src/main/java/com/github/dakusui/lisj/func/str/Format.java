package com.github.dakusui.lisj.func.str;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

import static com.github.dakusui.lisj.Basic.*;

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
    if (evaluatedParams.length > 1)
      args = ArrayUtils.subarray(evaluatedParams, 1, evaluatedParams.length);
    ret.value(String.format(format, args != null ? args : new Object[] {}));
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (length(params) < 1) {
      throw new IllegalArgumentException();
    }
    Utils.checknull(get(params, 0));
    return params;
  }
}
