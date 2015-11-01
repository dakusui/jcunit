package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;


import java.lang.reflect.Array;
import java.util.*;

/**
 * A utility class of JCUnit.
 * <p/>
 * In case there is a good library and I want to use the functionality of it in JCUnit, I
 * usually mimic it here (except {@code Preconditions} of Guava, it's in {@code Checks})
 * instead of adding dependency on it.
 * <p/>
 * This is because JCUnit's nature which should be able to be used for any other software
 * (at least as much as possible, I want to make it so).
 */
public class Utils {
  private Utils() {
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
    Form<Object, String> formatter = new Utils.Form<Object, String>() {
      @Override
      public String apply(Object in) {
        if (in == null)
          return null;
        //noinspection ConstantConditions (it's already checked)
        Class<?> toStringDeclaringClass = ReflectionUtils.getMethod(in.getClass(), "toString").getDeclaringClass();
        if (Object.class.equals(toStringDeclaringClass)) {
          return Utils.toString(in);
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
    return Utils.join("$", composeSimpleName(new LinkedList<String>(), Checks.checknotnull(c)).toArray());
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


  /**
   * Returns {@code true} if {@code v} and {@code} are equal,
   * {@code false} otherwise.
   */
  public static boolean eq(Object v, Object o) {
    if (v == null) {
      return o == null;
    }
    return v.equals(o);
  }

  public static <T> List<T> toList(T... elements) {
    return Arrays.asList(elements);
  }

  public static <T> List<T> newUnmodifiableList(List<T> elements) {
    return Collections.unmodifiableList(newList(elements));
  }

  public static <T> List<T> newList(List<T> elements) {
    return new ArrayList<T>(elements);
  }

  public static <T> List<T> newList() {
    return newList(Collections.<T>emptyList());
  }

  public static boolean deepEq(Object a, Object b) {
    if (a == null || b == null) {
      return b == a;
    }
    if (!a.getClass().isArray() || !b.getClass().isArray()) {
      return a.equals(b);
    }

    int lena = Array.getLength(a);
    if (lena != Array.getLength(b)) {
      return false;
    }
    if (!a.getClass().equals(b.getClass())) {
      return false;
    }
    for (int i = 0; i < lena; i++) {
      if (!deepEq(Array.get(a, i), Array.get(b, i)))
        return false;
    }
    return true;
  }


  public static <T> List<T> dedup(Iterable<T> in) {
    List<T> ret = new LinkedList<T>();
    for (T each : Checks.checknotnull(in)) {
      if (!ret.contains(each))
        ret.add(each);
    }
    return ret;
  }

  /**
   * Returns a list whose members are coming from a parameter list {@code in}, but
   * each of them appears only once.
   * <p/>
   * Note that this method is not efficient if the size of {@code in} is very big.
   * it is implemented only for internal use of JCUnit.
   *
   * @param in List to be made a singleton.
   */
  public static <T> List<T> singleton(List<T> in) {
    Checks.checknotnull(in);
    List<T> ret = new ArrayList<T>(in.size());
    for (T each : in) {
      if (ret.contains(each))
        continue;
      ret.add(each);
    }
    return ret;
  }

  public static <I, O> List<O> transform(Iterable<I> in, Form<I, O> form) {
    List<O> ret = new ArrayList<O>();
    for (I each : in) {
      ret.add(form.apply(each));
    }
    return ret;
  }

  public static <I, O> List<O> transform(I[] in, Form<I, O> form) {
    return transform(
        Arrays.asList(in),
        Checks.checknotnull(form)
    );
  }

  public static <K, V> Map<K, V> toMap(List<V> in, Form<V, K> form) {
    Checks.checknotnull(in);
    Checks.checknotnull(form);
    ////
    // In most cases, it's better to use LinkedHashMap in JCUnit because
    // it needs to guarantee the test case generation result the same always.
    // So this method returns LinkedHashMap instead of HashMap.
    Map<K, V> ret = new LinkedHashMap<K, V>();
    for (V each : in) {
      ret.put(form.apply(each), each);
    }
    return ret;
  }

  public static <V> List<V> filter(Iterable<V> unfiltered, Predicate<V> predicate) {
    Checks.checknotnull(unfiltered);
    Checks.checknotnull(predicate);
    List<V> ret = new LinkedList<V>();
    for (V each : unfiltered) {
      if (predicate.apply(each))
        ret.add(each);
    }
    return ret;
  }

  public static <T> T[] concatenate(T[] a, T[] b) {
    int aLen = a.length;
    int bLen = b.length;

    @SuppressWarnings("unchecked")
    T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);

    return c;
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

  public interface Form<I, O> {
    O apply(I in);
  }

  public interface Predicate<I> {
    boolean apply(I in);
  }
}
