package com.github.dakusui.lisj.special;

import static com.github.dakusui.lisj.Basic.get;
import static com.github.dakusui.lisj.Basic.length;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;

public class When extends BaseForm {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 2600006689489802965L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws JCUnitException, CUT {
    int pos = lastResult.nextPosition();
    FormResult ret = evaluateEachSimply(context, currentParam, lastResult);

    if (pos == 0) {
      if (ret.value() instanceof Boolean) {
        boolean res = (Boolean) ret.value();
        if (!res)
          cut(res);
      } else {
        throw new IllegalArgumentException(
            msgFirstParameterTypeMismatch(currentParam));
      }
    }
    return ret;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    return lastResult;
  }

  /*
   * The first parameter must be or return a boolean value. The rest can be
   * anything.
   * 
   * @see com.github.dakusui.lisj.BaseForm#checkParams(java.lang.Object[])
   */
  @Override
  protected Object checkParams(Object params) {
    if (length(super.checkParams(params)) < 1)
      throw new IllegalArgumentException();
    Utils.checknotnull(get(params, 0));
    return params;
  }

  @Override
  protected boolean throwsCUT() {
    return true;
  }
}
