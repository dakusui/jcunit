package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.*;

import java.util.LinkedList;
import java.util.TreeSet;

public class Not extends LogicalPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 8870334868634469182L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
    FormResult ret = super.evaluateEach(context, currentParam, lastResult);
    return ret;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;
    Object target = evaluatedParams[0];
    if (!(target instanceof Boolean)) {
      throw new IllegalArgumentException(msgReturnedTypeMismatch(Boolean.class,
          target));
    }
    ret.value(!((Boolean) target));
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (Basic.length(params) != 1) {
      throw new IllegalArgumentException();
    }
    return params;
  }

  @Override
  protected FormEvaluator newEvaluator(Context context, Object params) {
    /*
     * Since it's sure this class doesn't need to create child context,
     * FormEvaluator is created with incoming context.
     */
    return new FormEvaluator(context, this, checkParams(params),
        new FormResult(0, Basic.length(params), null));
  }

  @Override
  protected boolean initialValue() {
    // This value will never be used since an exception will be thrown instead
    // in case no parameters are given to this predicate.
    return false;
  }
}
