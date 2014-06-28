package com.github.dakusui.lisj.pred;

import static com.github.dakusui.lisj.Basic.length;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.*;

public abstract class BinomialPredicate extends BasePredicate {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 8449575741493862687L;

  @Override
  protected Object checkParams(Object params) {
    Utils.checknotnull(params);
    if (length(params) != 2)
      throw new IllegalArgumentException(msgParameterLengthWrong(2, params));
    return params;
  }

  @Override
  protected FormEvaluator newEvaluator(Context context, Object params) {
    /*
     * Since it's sure that the subclasses of this class doesn't need to create
     * child context, FormEvaluator is created with incoming context.
     */
    return new FormEvaluator(context, this, checkParams(params),
        new FormResult(0, Basic.length(params), null));
  }

}
