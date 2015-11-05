package com.github.dakusui.jcunit.core;


import com.github.dakusui.jcunit.fsm.State;

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
   * Returns {@code true} if {@code v} and {@code} are equal,
   * {@code false} otherwise.
   */
  public static boolean eq(Object v, Object o) {
    if (v == null) {
      return o == null;
    }
    return v.equals(o);
  }

  public static <T> List<T> asList(T... elements) {
    return Arrays.asList(elements);
  }

  public static <T> List<T> newUnmodifiableList(List<T> elements) {
    return Collections.unmodifiableList(newList(elements));
  }

  public static <T> List<T> newList(T... elements) {
    List<T> ret = new ArrayList<T>(elements.length);
    ret.addAll(asList(elements));
    return ret;
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

  /**
   * Returns a new list whose elements are coming from a parameter list {@code in}, but
   * each of them appears only once.
   * <p/>
   * Note that this method is not efficient if the size of {@code in} is very big.
   * it is implemented only for internal use of JCUnit.
   *
   * @param in List whose elements to be made  unique.
   */
  public static <T> List<T> dedup(List<T> in) {
    Checks.checknotnull(in);
    List<T> ret = new ArrayList<T>(in.size());
    for (T each : in) {
      if (ret.contains(each))
        continue;
      ret.add(each);
    }
    return ret;
  }

  /**
   * If a list {@code in} is immutable and efficient at index access, such as
   * {@code ArrayList}, which is recommended to use in JCUnit, consider using
   * this method.
   *
   * @param in   input list.
   * @param form A form to translate input to output
   * @param <I>  Input type
   * @param <O>  Output type
   */
  public static <I, O> List<O> transformLazily(final List<I> in, final Form<I, O> form) {
    return new AbstractList<O>() {
      @Override
      public O get(int index) {
        return form.apply(in.get(index));
      }

      @Override
      public int size() {
        return in.size();
      }
    };
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

  public static <K, V> Map<K, V> newMap() {
    return newMap(Collections.<K, V>emptyMap());
  }

  public static <K, V> Map<K, V> newMap(Map<K, V> from) {
    return new LinkedHashMap<K, V>(from);
  }

  public interface Form<I, O> {
    O apply(I in);
  }

  public interface Predicate<I> {
    boolean apply(I in);
  }
}
