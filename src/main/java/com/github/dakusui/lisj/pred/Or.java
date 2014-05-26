package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Or extends LogicalPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 6418029016956795143L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {

    try {
      FormResult ret = super.evaluateEach(context, currentParam, lastResult);
      if (ret.value() instanceof Boolean) {
        if (((Boolean) ret.value()))
          cut(true);
      } else {
        throw new IllegalArgumentException(msgReturnedTypeMismatch(Boolean.class,
            ret.value()));
      }
      return ret;
    } catch (CUT cut) {
      throw cut;
    }
  }

  @Override
  protected boolean initialValue() {
    return false;
  }
}
