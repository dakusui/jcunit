package com.github.dakusui.jcunit.core.reflect;

import com.github.dakusui.jcunit.core.utils.Checks;

import java.lang.reflect.Method;


public enum ReflectionUtils {
  ;
  public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
    try {
      return Checks.checknotnull(clazz).getMethod(Checks.checknotnull(methodName), params);
    } catch (NoSuchMethodException e) {
      throw Checks.wrap(e);
    }
  }
}
