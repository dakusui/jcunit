package com.github.dakusui.lisj.special;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.*;

public class Cond extends BaseForm {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -7534444891858202067L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitCheckedException, CUT {
    boolean successfullyEvaluated = false;
    FormResult ret = lastResult;
    try {
      ret = evaluateEachSimply(context, currentParam, lastResult);
      successfullyEvaluated = true;
    } catch (CUT e) {
      if (e.source() == Basic.car(currentParam)) {
        ret.value(e.value());
      } else {
        // some other underlying form/predicate threw a CUT.
        throw e;
      }
    } finally {
      if (successfullyEvaluated) {
        cut(ret.value());
      }
    }
    return ret;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    return lastResult;
  }
}
