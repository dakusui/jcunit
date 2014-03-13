package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Eq extends BinomialPredicate {
  /**
   * Serial version UID
   */
  private static final long serialVersionUID = -4792477732433911082L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitException, CUT {
    Object obj = evaluatedParams[0];
    Object another = evaluatedParams[1];
    lastResult.value(Basic.eq(obj, another));
    return lastResult;
  }
}
