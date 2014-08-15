package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitParameterException;
import com.github.dakusui.jcunit.exceptions.JCUnitPluginException;
import sun.security.krb5.Config;

import java.lang.annotation.Annotation;
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
    if (t instanceof Error) throw (Error)t;
    if (t instanceof RuntimeException) throw (RuntimeException)t;
    throw new JCUnitParameterException(String.format(msg, args), t);
  }

  public static Object[] processParams(ParamType[] types, Param[] params) {
    Utils.checknotnull(params);
    Utils.checknotnull(types);
    int minLength = types.length;
    if (minLength > 0 && types[minLength - 1].isVarArgs()) minLength--;
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
    Object[] ret = new Object[Math.max(types.length, params.length)];
    int i = 0;
    boolean varArgsDefined = false;
    boolean varArgsParameterPresent = false;
    for (ParamType t : types) {
        if (i >= params.length) {
          if (t.hasDefaultValue()) {
            ret[i] = t.defaultValue();
          } else if (t.isVarArgs()) {
            ConfigUtils.checkPlugIn(i == types.length - 1, "Var args parameter can only be placed at the last of parameters.");
            varArgsDefined = true;
            break;
          } else {
            ConfigUtils.checkPlugIn(false, "Failed to parse %s (%s) in %d", Arrays.toString(params), Arrays.toString(types), i);
          }
        } else {
          try {
            if (!t.isVarArgs()) {
              ret[i] = t.parse(params[i].value());
            } else {
              ConfigUtils.checkPlugIn(i == types.length - 1, "Var args parameter can only be placed at the last of parameters.");
              varArgsDefined = true;
              while (i < params.length) {
                ret[i] = t.parse(params[i].value());
                varArgsParameterPresent = true;
                i++;
              }
              break;
            }
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
    if (varArgsDefined && !varArgsParameterPresent) {
      ////
      // In case the last param is var args and no value is given to
      // it, nothing should be appended to the returned parameter values.
      ret = Arrays.copyOfRange(ret, 0, ret.length - 1);
    }
    return ret;
  }
}
