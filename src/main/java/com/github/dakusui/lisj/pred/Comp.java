package com.github.dakusui.lisj.pred;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

import static com.github.dakusui.lisj.Basic.get;

public abstract class Comp extends BinomialPredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 234943933153946011L;

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    Utils.checknotnull(get(params, 0));
    Utils.checknotnull(get(params, 1));
    return params;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;

    Object value = evaluatedParams[0];
    Object another = evaluatedParams[1];

    if (value == null || !(value instanceof Comparable)) {
      throw new IllegalArgumentException(msgIllegalArgumentFound(value,
          evaluatedParams));
    }
    if (another == null || !(another instanceof Comparable)) {
      throw new IllegalArgumentException(msgIllegalArgumentFound(another,
          evaluatedParams));
    }

    ret.value(evaluate(value, another));
    return ret;
  }

  protected abstract boolean evaluate(Object value, Object another);

  @SuppressWarnings("unchecked")
  protected int compare(@SuppressWarnings("rawtypes") Comparable a,
      @SuppressWarnings("rawtypes") Comparable b) {
    return a.compareTo(b);
  }
}
