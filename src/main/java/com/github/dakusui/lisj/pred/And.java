package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class And extends LogicalMultinominalPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -3809947834203544114L;

  @Override
  protected boolean initialValue() {
    return true;
  }

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitCheckedException, CUT {
    return evaluateEach(false, context, currentParam, lastResult);
  }
}
