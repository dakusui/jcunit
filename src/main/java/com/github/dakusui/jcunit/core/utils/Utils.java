package com.github.dakusui.jcunit.core.utils;


import java.lang.reflect.Array;
import java.util.*;

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

  private static final Predicate ALWAYS_TRUE = new Predicate() {
    @Override
    public boolean apply(Object in) {
      return true;
    }
  };

  public static <T> Predicate<T> not(final Predicate<T> predicate) {
    checknotnull(predicate);
    return new Predicate<T>() {
      @Override
      public boolean apply(T in) {
        return !predicate.apply(in);
      }
    };
  }

  public static <T> Predicate<T> alwaysTrue() {
    //noinspection unchecked
    return (Predicate<T>) ALWAYS_TRUE;
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
    checknotnull(in);
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

  public static <I, O> List<O> transform(Iterable<? extends I> in, Form<I, O> form) {
    List<O> ret = new ArrayList<O>();
    for (I each : in) {
      ret.add(form.apply(each));
    }
    return ret;
  }

  public static <I, O> List<O> transform(I[] in, Form<I, O> form) {
    return transform(
        Arrays.asList(in),
        checknotnull(form)
    );
  }

  public static <V> List<V> filter(Iterable<V> unfiltered, Predicate<V> predicate) {
    checknotnull(unfiltered);
    checknotnull(predicate);
    List<V> ret = new LinkedList<V>();
    for (V each : unfiltered) {
      if (predicate.apply(each))
        ret.add(each);
    }
    return ret;
  }

  public static <T> boolean containsAny(List<T> a, List<T> b) {
    boolean ret = false;
    for (T outer : a) {
      for (T inner : b) {
        if (Utils.eq(outer, inner))
          return true;
      }
    }
    return false;
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

  public static <T> List<T> concatenate(List<T> a, T... b) {
    List<T> ret = new LinkedList<T>(a);
    ret.addAll(Arrays.asList(b));
    return ret;
  }

  public static <E> List<E> sort(List<E> list, Comparator<? super E> by) {
    Collections.sort(list, by);
    return list;
  }

  public static <K, V> Map<K, V> toMap(List<V> in, Form<V, K> form) {
    checknotnull(in);
    checknotnull(form);
    ////
    // In most cases, it's better to use LinkedHashMap in JCUnit because
    // it needs to guarantee the test case generation generatedTuples the same always.
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

  public static <E> Set<E> newSet() {
    return new LinkedHashSet<E>();
  }

  public static <T> List<T> asList(T... elements) {
    return Arrays.asList(elements);
  }

  public static <T> List<T> newUnmodifiableList(List<? extends T> elements) {
    //noinspection RedundantTypeArguments
    return Collections.<T>unmodifiableList(newList(elements));
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

  public static <T> List<T> toList(LinkedHashSet<T> set) {
    return new LinkedList<T>(set);
  }

  public static <T> LinkedHashSet<T> toLinkedHashSet(List<T> list) {
    return new LinkedHashSet<T>(list);
  }

  public interface Form<I, O> {
    O apply(I in);
  }

  public interface Predicate<I> {
    boolean apply(I in);
  }

  public interface Consumer<I> {
    void accept(I t);
  }


  public static abstract class By<I> implements Form<I, Object>, Comparator<I> {
    @Override
    public abstract Comparable apply(I in);

    @Override
    public int compare(I o1, I o2) {
      return apply(o1).compareTo(apply(o2));
    }
  }
}
