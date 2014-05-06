package com.github.dakusui.lisj.special;

import org.apache.commons.lang3.ArrayUtils;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.Symbol;

public class Lambda extends BaseForm {

  /**
   * Serial version UID
   */
  private static final long serialVersionUID = 7978574769583650805L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam, FormResult lastResult)
      throws JCUnitException, CUT {
    lastResult.incrementPosition();
    lastResult.value(currentParam);
    return lastResult;
  }

  @Override
  protected FormResult evaluateLast(Context context, final Object[] evaluatedParams, FormResult lastResult)
      throws JCUnitException, CUT {
    FormResult ret = lastResult;

    final Symbol[] paramSymbols = Utils.cast(Symbol[].class, Utils.checknull(evaluatedParams[0]));
    final Object[] funcBody = ArrayUtils.subarray(evaluatedParams, 1, evaluatedParams.length);

    ret.value(new BaseForm() {
      private static final long serialVersionUID = 1L;

      @Override
      protected FormResult evaluateEach(Context context, Object currentParam, FormResult lastResult)
          throws JCUnitException, CUT {
        FormResult ret = lastResult;

        ret.value(context.bind(paramSymbols[lastResult.nextPosition()], currentParam));
        ret.incrementPosition();

        return ret;
      }

      @Override
      protected FormResult evaluateLast(Context context, Object[] evaluatedParams, FormResult lastResult)
          throws JCUnitException, CUT {
        FormResult ret = lastResult;
        for (Object cur : funcBody) {
          ret = evaluateEachSimply(context, cur, ret);
        }
        return ret;
      }

      @Override
      public String name() {
        return "(lambda)";
      }
    });
    return ret;
  }
}
