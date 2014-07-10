package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Or extends LogicalMultinominalPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 6418029016956795143L;

  @Override
  protected boolean initialValue() {
    return false;
  }

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitCheckedException, CUT {
    return evaluateEach(true, context, currentParam, lastResult);
  }
}
