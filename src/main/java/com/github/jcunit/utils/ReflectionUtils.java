package com.github.jcunit.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


public enum ReflectionUtils {
  ;

  @SuppressWarnings("unchecked")
  public static <V> V invoke(Object instance, Method method, Object... args) {
    try {
      return (V) method.invoke(instance, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }
}
