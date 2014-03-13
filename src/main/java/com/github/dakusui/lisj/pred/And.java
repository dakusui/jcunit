package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class And extends LogicalPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -3809947834203544114L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
    FormResult ret = super.evaluateEach(context, currentParam, lastResult);
    if (ret.value() instanceof Boolean) {
      if (!((Boolean) ret.value()))
        cut(false);
    } else {
      throw new IllegalArgumentException(msgReturnedTypeMismatch(Boolean.class,
          ret.value()));
    }
    return ret;
  }

  @Override
  protected boolean initialValue() {
    return true;
  }
}
