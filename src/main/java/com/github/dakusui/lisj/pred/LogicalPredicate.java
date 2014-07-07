package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.*;

public abstract class LogicalPredicate extends BasePredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -1779614522843256950L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
    FormResult ret = super.evaluateEach(context, currentParam, lastResult);
    return ret;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitException {
    return lastResult;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    for (Object cur : Basic.iterator(params)) {
      if (cur == null) {
        throw new IllegalArgumentException();
      }
    }
    return params;
  }

  @Override
  protected FormEvaluator newEvaluator(Context context, Object params) {
    /*
     * Since it's sure that the subclasses of this class doesn't need to create
     * child context, FormEvaluator is created with incoming context.
     */
    return new FormEvaluator(context, this, checkParams(params),
        new FormResult(0, Basic.length(params), initialValue()));
  }

  /*
   * Implementations of this class must returns a boolean value which will be
   * used when no parameters are given to this predicate.
   */
  abstract protected boolean initialValue();
}
