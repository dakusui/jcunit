package com.github.jcunit.utils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;


public enum ReflectionUtils {
  ;

  @SuppressWarnings("unchecked")
  public static <V> V invoke(Object instance, Method method, Object... args) {
    try {
      return (V) method.invoke(instance, args);
    } catch (IllegalArgumentException | IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(String.format("Failed to execute '%s' on '%s' with %s", method, instance, Arrays.asList(args)), e);
    }
  }
}
