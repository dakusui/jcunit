package com.github.dakusui.lisj.special;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.*;
import org.apache.commons.lang3.ArrayUtils;

import static com.github.dakusui.lisj.Basic.length;

public class Eval extends BaseForm {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -6700796596065088982L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitCheckedException, CUT {
    return evaluateEachSimply(context, currentParam, lastResult);
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitCheckedException, CUT {
    FormResult ret = lastResult;

    if (evaluatedParams[0] instanceof Form) {
      Form form = (Form) evaluatedParams[0];
      Object[] cdr = ArrayUtils.subarray(evaluatedParams, 1,
          evaluatedParams.length);
      ret.value(form.evaluate(context, cdr));
    } else {
      // //
      // It is guaranteed that more there are than one parameters by checkParams
      // method.
      ret.value(evaluatedParams[evaluatedParams.length - 1]);
    }
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    if (length(super.checkParams(params)) < 1) {
      throw new IllegalArgumentException(
          msgParameterLengthWrong(">=0", params));
    }
    return params;
  }
}
