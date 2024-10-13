package com.github.jcunit.utils;

import com.github.jcunit.core.tuples.Tuple;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

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
  
  public static PrintStream out = System.out;
  
  public static <T> Function<T, T> printer() {
    return printer(Object::toString);
  }
  
  public static <T> Function<T, T> printer(Function<T, String> formatter) {
    return t -> {
      System.out.println(formatter.apply(t));
      return t;
    };
  }
  
  @SuppressWarnings("unchecked")
  public static <T> T print(T data) {
    return (T) printer().apply(data);
  }
  
  public static <T> List<T> unique(List<T> in) {
    return new ArrayList<>(new LinkedHashSet<>(in));
  }
  
  public static <T> int sizeOfIntersection(Set<T> a, Set<T> b) {
    Set<T> lhs;
    Set<T> rhs;
    if (a.size() > b.size()) {
      lhs = b;
      rhs = a;
    } else {
      lhs = a;
      rhs = b;
    }
    int ret = 0;
    for (T each : lhs) {
      if (rhs.contains(each))
        ret++;
    }
    return ret;
  }
  
  public static Tuple project(List<String> keys, Tuple from) {
    Tuple.Builder builder = new Tuple.Builder();
    keys.forEach((String key) -> builder.put(key, from.get(key)));
    return builder.build();
  }

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

  public static String className(Class<?> klass) {
    return className(klass, "");
  }

  private static String className(Class<?> klass, String work) {
    String canonicalName = klass.getCanonicalName();
    if (canonicalName != null)
      return canonicalName;
    return className(klass.getEnclosingClass(), work + "$");
  }

  
  public static <T extends Predicate<E>, E> Predicate<E> conjunct(Iterable<T> predicates) {
    Predicate<E> ret = tuple -> true;
    for (Predicate<E> each : predicates) {
      ret = ret.and(each);
    }
    return ret;
  }
  
  public static <T, R> Function<T, R> memoize(Function<T, R> function) {
    Map<T, R> memo = new ConcurrentHashMap<>();
    return t -> memo.computeIfAbsent(t, function);
  }

  public static <T> List<T> concat(List<T> var, List<T> vars) {
    return Stream.concat(var.stream(), vars.stream()).collect(toList());
  }

  @SafeVarargs
  public static <T> List<T> concatenate(List<T> a, T... b) {
    List<T> ret = new LinkedList<>(a);
    ret.addAll(Arrays.asList(b));
    return ret;
  }

  public static String joinBy(String sep, List<?> elems) {
    return elems.stream().map(Objects::toString).collect(joining(sep));
  }
}
