package com.github.dakusui.lisj.func;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public abstract class BaseFunc extends BaseForm {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -5433964838210243390L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitCheckedException, CUT {
    return evaluateEachSimply(context, currentParam, lastResult);
  }
}
