package com.github.dakusui.lisj.pred;

import static com.github.dakusui.lisj.Basic.get;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Contains extends BinomialPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 6765312187444622131L;

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    Utils.checknull(get(params, 0));
    Utils.checknull(get(params, 1));
    return params;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;

    String value = evaluatedParams[0].toString();

    String s = evaluatedParams[1].toString();

    ret.value(value.contains(s));

    return ret;
  }
}
