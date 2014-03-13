package com.github.dakusui.lisj.special;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.Form;
import com.github.dakusui.lisj.FormResult;

import static com.github.dakusui.lisj.Basic.*;

public class Eval extends BaseForm {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -6700796596065088982L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
    return evaluateEachSimply(context, currentParam, lastResult);
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitException, CUT {
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
      throw new IllegalArgumentException(msgParameterLengthWrong(">=0", params));
    }
    return params;
  }
}
