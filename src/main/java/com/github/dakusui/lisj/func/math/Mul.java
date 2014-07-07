package com.github.dakusui.lisj.func.math;

import com.github.dakusui.lisj.Context;

import java.math.BigDecimal;

public class Mul extends NumericFunc {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 8059283467791114034L;

  @Override
  protected BigDecimal bigDecimalsEvaluateLast(Context context,
      BigDecimal[] evaluatedParams) {
    BigDecimal ret = BigDecimal.ONE;
    for (BigDecimal cur : evaluatedParams) {
      if (ret == null) {
        ret = cur;
      } else {
        ret = ret.multiply(cur);
      }
    }
    return ret;
  }

}
