package com.github.dakusui.lisj.special;

import static com.github.dakusui.lisj.Basic.get;
import static com.github.dakusui.lisj.Basic.length;

import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.BaseForm;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.Symbol;

public class Assign extends BaseForm {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 4109334578076480349L;

  @Override
  protected FormResult evaluateEach(Context context, Object currentParam, FormResult lastResult)
      throws JCUnitException, CUT {
    FormResult ret = lastResult;

    if (ret.nextPosition() == 0) {
      ret.incrementPosition();
      ret.value(currentParam);
      return ret;
    }
    ret.value(evaluateEachSimply(context, currentParam, lastResult).value());

    return ret;
  }

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams, FormResult lastResult)
      throws JCUnitException {
    FormResult ret = lastResult;

    Symbol symbol = (Symbol) evaluatedParams[0];
    Object value = evaluatedParams[1];

    context.bind(symbol, value);
    return ret;
  }

  @Override
  public Object checkParams(Object params) {
    super.checkParams(params);
    if (length(params) != 2)
      throw new IllegalArgumentException(msgParameterLengthWrong(2, params));
    if (!(get(params, 0) instanceof Symbol))
      throw new IllegalArgumentException();
    return params;
  }
}
