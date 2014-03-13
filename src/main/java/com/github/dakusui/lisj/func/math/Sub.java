package com.github.dakusui.lisj.func.math;

import java.math.BigDecimal;

import com.github.dakusui.lisj.Context;

import static com.github.dakusui.lisj.Basic.*;

public class Sub extends NumericFunc {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 30455154598602132L;

  @Override
  protected BigDecimal bigDecimalsEvaluateLast(Context context,
      BigDecimal[] evaluatedParams) {
    BigDecimal ret = null;
    for (BigDecimal cur : evaluatedParams) {
      if (ret == null)
        ret = cur;
      else
        ret = ret.subtract(cur);
    }
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (length(params) < 1)
      throw new IllegalArgumentException(tooFewArguments(params));
    return params;
  }

  private String tooFewArguments(Object params) {
    return String.format("Too few arguments(%s) are given.", tostr(params));
  }
}
