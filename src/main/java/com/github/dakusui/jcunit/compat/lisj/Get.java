package com.github.dakusui.jcunit.compat.lisj;

import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.LisjUtils;
import com.github.dakusui.lisj.func.BaseFunc;

public class Get extends BaseFunc {
  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = 9107185518447270811L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) {
    FormResult ret = lastResult;
    Object obj = Utils.checknotnull(evaluatedParams[0]);
    String attrName = Utils.checknotnull(evaluatedParams[1]).toString();
    ret.value(LisjUtils.normalize(Utils.getFieldValue(obj,
        Utils.getField(obj, attrName, Out.class, In.class))));
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (Basic.length(params) != 2) {
      throw new IllegalArgumentException();
    }
    Utils.checknotnull(Basic.get(params, 0));
    Utils.checknotnull(Basic.get(params, 1));
    return params;
  }
}
