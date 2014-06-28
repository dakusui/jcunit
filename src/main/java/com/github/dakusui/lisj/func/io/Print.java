package com.github.dakusui.lisj.func.io;

import java.io.PrintStream;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

import static com.github.dakusui.lisj.Basic.*;

public class Print extends BaseFunc {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -2851340410686011195L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;
    PrintStream pw = Utils.cast(PrintStream.class,
        Utils.checknotnull(evaluatedParams[0]));
    String s = Utils.checknotnull(evaluatedParams[1]).toString();
    pw.print(s);
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    if (length(super.checkParams(params)) != 2)
      throw new IllegalArgumentException();
    Utils.checknotnull(get(params, 0));
    return params;
  }
}
