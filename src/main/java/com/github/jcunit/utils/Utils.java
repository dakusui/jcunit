package com.github.jcunit.utils;


import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

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
    List<T> ret = new LinkedList<>(a);
    ret.addAll(Arrays.asList(b));
    return ret;
  }
}
