package com.github.dakusui.lisj.func.math;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.LisjUtils;
import com.github.dakusui.lisj.func.BaseFunc;

import java.math.BigDecimal;
import java.util.Iterator;

import static com.github.dakusui.lisj.Basic.iterator;

public abstract class NumericFunc extends BaseFunc {
  private static final long serialVersionUID = -4320393403606841222L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;

    BigDecimal[] evaluatedParamsBigDecimal = new BigDecimal[evaluatedParams.length];
    int i = 0;
    for (Object cur : evaluatedParams) {
      if (cur instanceof Number) {
        evaluatedParamsBigDecimal[i] = LisjUtils.bigDecimal((Number) cur);
      } else {
        String message = String.format("Given value %s(%s) isn't a number.",
            cur, cur != null ? cur.getClass() : null);
        throw new ObjectUnderFrameworkException(message, null);
      }
      i++;
    }
    ret.value(bigDecimalsEvaluateLast(context, evaluatedParamsBigDecimal));
    return ret;
  }

  abstract protected BigDecimal bigDecimalsEvaluateLast(Context context,
      BigDecimal[] evaluatedParams);

  @Override
  protected Object checkParams(Object params) {
    Iterator<Object> i = iterator(params);
    while (i.hasNext()) {
      Object cur = i.next();
      Utils.checknotnull(cur);
    }
    return params;
  }
}
