package com.github.dakusui.lisj.special;

import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;
import com.github.dakusui.lisj.*;

import static com.github.dakusui.lisj.Basic.eval;
import static com.github.dakusui.lisj.Basic.length;

public class Loop extends BaseForm {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 1431934849070993256L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] params,
      FormResult lastResult) {
    return lastResult;
  }

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam,
      FormResult lastResult) throws LisjCheckedException, CUT {
    currentParam = eval(context, currentParam);
    if (lastResult.nextPosition() == 0) {
      if ((currentParam instanceof Boolean)) {
        if (!(Boolean) currentParam) {
          cut(false);
        }
      } else {
        String msg = String
            .format(
                "The first parameter of this (%s) object must be a boolean when it is evaluated but '%s'.",
                this, Basic.tostr(currentParam));
        throw new ObjectUnderFrameworkException(msg, null);
      }
    }
    FormResult ret = lastResult;
    ret.value(currentParam);
    ret.incrementPosition();
    if (ret.nextPosition() >= ret.numPositions()) {
      ret.nextPosition(0);
    }
    return ret;
  }

  /*
   * The first parameter must be or return a boolean value. The other parameters
   * can be anything.
   * 
   * @see com.github.dakusui.lisj.BaseForm#checkParams(java.lang.Object[])
   */
  @Override
  protected Object checkParams(Object params) {
    if (length(super.checkParams(params)) < 1) {
      throw new IllegalArgumentException();
    }
    return params;
  }
}
