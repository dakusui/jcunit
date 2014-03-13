package com.github.dakusui.lisj.special;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Cond extends BaseForm {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -7534444891858202067L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
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
      if (successfullyEvaluated)
        cut(ret.value());
    }
    return ret;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    return lastResult;
  }
}
