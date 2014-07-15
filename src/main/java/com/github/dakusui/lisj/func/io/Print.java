package com.github.dakusui.lisj.func.io;

import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.LisjUtils;
import com.github.dakusui.lisj.func.BaseFunc;

import java.io.PrintStream;

import static com.github.dakusui.lisj.Basic.get;
import static com.github.dakusui.lisj.Basic.length;

public class Print extends BaseFunc {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -2851340410686011195L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;
    PrintStream pw = LisjUtils.cast(PrintStream.class,
		    LisjUtils.checknotnull(evaluatedParams[0]));
    String s = LisjUtils.checknotnull(evaluatedParams[1]).toString();
    pw.print(s);
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    if (length(super.checkParams(params)) != 2) {
      throw new IllegalArgumentException();
    }
	  LisjUtils.checknotnull(get(params, 0));
    return params;
  }
}
