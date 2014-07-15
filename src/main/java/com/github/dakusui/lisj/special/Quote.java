package com.github.dakusui.lisj.special;

import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class Quote extends BaseForm {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = -7817074688838146836L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws LisjCheckedException, CUT {
    FormResult ret = lastResult;
    ret.value(currentParam);
    ret.incrementPosition();
    return ret;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws LisjCheckedException {
    FormResult ret = lastResult;
    ret.value(evaluatedParams);
    return ret;
  }

}
