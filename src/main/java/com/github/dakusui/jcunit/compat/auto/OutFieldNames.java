package com.github.dakusui.jcunit.compat.auto;

import com.github.dakusui.jcunit.compat.core.CompatUtils;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.lisj.Basic;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.Context;
import com.github.dakusui.lisj.FormResult;
import com.github.dakusui.lisj.func.BaseFunc;

import java.lang.reflect.Field;

public class OutFieldNames extends BaseFunc {

  /**
   * Serial version UID.
   */
  private static final long serialVersionUID = -7996561462620874712L;

  @Override
  protected FormResult evaluateLast(Context context, Object[] evaluatedParams,
      FormResult lastResult) throws JCUnitCheckedException, CUT {
    FormResult ret = lastResult;
    Field[] outFields = CompatUtils.getOutFieldsFromClassUnderTest(Basic.get(
		    evaluatedParams, 0).getClass());
    // //
    // outFields can never be null since 'checkParams' guarantees it.
    if (outFields.length == 0) {
      ret.value(Basic.NIL);
    } else if (outFields.length == 1) {
      ret.value(Basic.cons(outFields[0].getName(), Basic.NIL));
    } else {
      Object[] fieldNames = new Object[outFields.length];
      int i = 0;
      for (Field f : outFields) {
        fieldNames[i] = f.getName();
        i++;
      }
      ret.value(fieldNames);
    }
    return ret;
  }

  @Override
  protected Object checkParams(Object params) {
    super.checkParams(params);
    if (Basic.length(params) != 1) {
      throw new IllegalArgumentException();
    }
    Utils.checknotnull(Basic.get(params, 0));
    return params;
  }
}
