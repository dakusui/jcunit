package com.github.dakusui.lisj.special;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Progn extends BaseForm {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 1012273507783010986L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
    return evaluateEachSimply(context, currentParam, lastResult);
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitException {
    return lastResult;
  }

}
