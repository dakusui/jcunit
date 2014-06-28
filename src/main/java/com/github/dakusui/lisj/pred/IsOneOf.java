package com.github.dakusui.lisj.pred;

import static com.github.dakusui.lisj.Basic.length;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class IsOneOf extends BinomialPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -2030221966727213413L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;

    Object obj = evaluatedParams[0];

    for (Object cur : ArrayUtils.subarray(evaluatedParams, 1,
        evaluatedParams.length)) {
      // //
      // Normalize number objects to 'BigDecimal' if they are Numbers.
      cur = Utils.normalize(cur);
      if (obj == null) {
        if (cur == null) {
          ret.value(true);
          return ret;
        } else {
          continue;
        }
      }
      if (obj.equals(cur)) {
        ret.value(true);
        return ret;
      }
    }
    ret.value(false);
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    Utils.checknotnull(params);
    if (length(params) < 2) {
      throw new IllegalArgumentException(msgParameterLengthWrong(">1",
          Basic.tostr(params)));
    }
    return params;
  }

}
