package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;

import java.util.LinkedList;
import java.util.List;

public class StringUtils {
  private StringUtils() {
  }

  /**
   * Returns a user friendly string using given format and arguments.
   *
   * Should only be used for messages directly shown to users because this method
   * does relatively heavy conversions to keep the returned string concise and
   * informative.
   * Also note that unlike {@code String#format}, only '%s' specifier is supported.
   *
   * @param format A format string.
   * @param args Arguments referenced by the format specifier in the format string.
   */
  public static String format(String format, Object... args) {
    Utils.Form<Object, String> formatter = new Utils.Form<Object, String>() {
      @Override
      public String apply(Object in) {
        if (in == null)
          return null;
        //noinspection ConstantConditions (it's already checked)
        Class<?> toStringDeclaringClass = ReflectionUtils.getMethod(in.getClass(), "toString").getDeclaringClass();
        if (Object.class.equals(toStringDeclaringClass)) {
          return StringUtils.toString(in);
        } else if (in instanceof Class) {
          return getSimpleName((Class) in);
        }
        return in.toString();
      }

    };
    return String.format(
        Checks.checknotnull(format),
        Utils.transform(args, formatter).toArray());
  }

  public static String toString(Object o) {
    if (o == null)
      return null;
    if (o instanceof Class) {
      return getSimpleName((Class) o);
    }
    return getSimpleName(o.getClass()) + "@" + System.identityHashCode(o);
  }

  public static String getSimpleName(Class c) {
    return join("$", composeSimpleName(new LinkedList<String>(), Checks.checknotnull(c)).toArray());
  }

  private static List<String> composeSimpleName(List<String> classNest, Class c) {
    if (c == null)
      return classNest;
    classNest.add(0, c.getSimpleName());
    return composeSimpleName(classNest, c.getEnclosingClass());
  }

  /**
   * Joins given string objects with {@code sep} using {@code formatter}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep       A separator to be used to join {@code elemes}.
   * @param formatter A formatter used to join strings.
   * @param elems     Elements to be joined.
   * @return A joined {@code String}
   */
  public static <T> String join(String sep, Formatter<T> formatter,
      T... elems) {
    Checks.checknotnull(sep);
    StringBuilder b = new StringBuilder();
    boolean firstOne = true;
    for (T s : elems) {
      if (!firstOne) {
        b.append(sep);
      }
      b.append(formatter.format(s));
      firstOne = false;
    }
    return b.toString();
  }

  /**
   * Joins given string objects with {@code sep} using {@code Formatter.INSTANCE}.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep   A separator to be used to join {@code elemes}
   * @param elems Elements to be joined.
   * @return A joined {@code String}
   */
  public static String join(String sep, Object... elems) {
    return join(sep, Formatter.INSTANCE, elems);
  }

  public static String join(String sep, List<?> elems) {
    return join(sep, Formatter.INSTANCE, Checks.checknotnull(elems).toArray());
  }


  public interface Formatter<T> {
    Formatter INSTANCE = new Formatter<Object>() {
      @Override
      public String format(Object elem) {
        if (elem == null) {
          return null;
        }
        return elem.toString();
      }
    };

    String format(T elem);
  }
}
