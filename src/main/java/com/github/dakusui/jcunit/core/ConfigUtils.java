package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitParameterException;
import com.github.dakusui.jcunit.exceptions.JCUnitPluginException;

import java.util.Arrays;
import java.util.List;

public class ConfigUtils {
  public static void checkParam(boolean cond, String msg,
      Object... args) {
    if (!cond) {
      throw new JCUnitParameterException(String.format(msg, args));
    }
  }

  private static void checkPlugIn(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new JCUnitPluginException(String.format(msg, args));
    }
  }

  public static void checkEnv(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new JCUnitEnvironmentException(String.format(msg, args));
    }
  }

  public static void checkTest(boolean cond, String msg, Object... args) {
    if (!cond) {
      throw new JCUnitEnvironmentException(String.format(msg, args));
    }
  }

  public static void rethrow(Throwable t, String msg, Object... args) {
    RuntimeException e = new JCUnitParameterException(String.format(msg, args), t);
    e.setStackTrace(t.getStackTrace());
    throw e;
  }

  public static Object[] processParams(ParamType[] types, Param[] params) {
    Utils.checknotnull(params);
    Utils.checknotnull(types);
    int minLength = types.length;
    while (minLength > 0) {
      if (types[minLength - 1].hasDefaultValue()) minLength--;
      else break;
    }
    for (int i = 0; i < minLength; i++)
      checkPlugIn(!types[i].hasDefaultValue(),
          "Only the last parameters of a plugin can have default values.: %s",
          Arrays.toString(types));
    checkParam(minLength <= params.length && params.length <= types.length,
        "Too little or too many number of parameters (at least %d and %d at maximum required, but %d given).: %s",
        minLength,
        types.length,
        params.length,
        Arrays.toString(params)
    );
    Object[] ret = new Object[types.length];
    int i = 0;
    for (ParamType t : types) {
        if (i >= params.length) {
          ret[i] = t.defaultValue();
        } else {
          try {
            ret[i] = t.parse(params[i].value());
          } catch (Exception e) {
            ConfigUtils.rethrow(e,
                String.format("The given value '%s' can't be converted to '%s' value.: %dth value in %s",
                    Arrays.toString(params[i].value()),
                    types[i],
                    i,
                    Arrays.toString(params)
                ));
          }
        }
      i++;
    }
    return ret;
  }
}
