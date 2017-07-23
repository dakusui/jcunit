package com.github.dakusui.jcunit.core.utils;


import java.lang.reflect.Array;
import java.util.*;
import java.util.function.Function;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;

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
public enum Utils {
  ;

  public static <T> T[] concatenate(T[] a, T[] b) {
    int aLen = a.length;
    int bLen = b.length;

    @SuppressWarnings("unchecked")
    T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);

    return c;
  }

  @SafeVarargs
  public static <T> List<T> concatenate(List<T> a, T... b) {
    List<T> ret = new LinkedList<T>(a);
    ret.addAll(Arrays.asList(b));
    return ret;
  }

  public static <E> List<E> sort(List<E> list, Comparator<? super E> by) {
    list.sort(by);
    return list;
  }

  public static <K, V> Map<K, V> toMap(List<V> in, Function<V, K> form) {
    checknotnull(in);
    checknotnull(form);
    ////
    // In most cases, it's better to use LinkedHashMap in JCUnit because
    // it needs to guarantee the test case generation generatedTuples the same always.
    // So this method returns LinkedHashMap instead of HashMap.
    Map<K, V> ret = new LinkedHashMap<>();
    for (V each : in) {
      ret.put(form.apply(each), each);
    }
    return ret;
  }

}
