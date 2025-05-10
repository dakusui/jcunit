package com.github.dakusui.jcunit.utils;


/**
 * // @formatter:off
 *
 * A utility class of JCUnit.
 *
 * In case there is a good library and I want to use the functionality of it in JCUnit, I
 * usually mimic it here (except {@code Preconditions} of Guava, it's in {@code Checks})
 * instead of adding dependency on it.
 *
 * This is because JCUnit's nature which should be able to be used for any other software
 * (at least as much as possible, I want to make it so).
 *
 * // @formatter:on
 * */
public enum InternalUtils {
  ;

  public static String simpleClassName(Class<?> klass) {
    return simpleClassName(klass, "");
  }

  private static String simpleClassName(Class<?> klass, String work) {
    String simpleName = klass.getSimpleName();
    if (isEmptyOrNull(simpleName))
      return simpleName;
    return simpleClassName(klass.getEnclosingClass(), work + "$");
  }

  public static boolean isEmptyOrNull(String string) {
    return string == null || string.isEmpty();
  }

}
