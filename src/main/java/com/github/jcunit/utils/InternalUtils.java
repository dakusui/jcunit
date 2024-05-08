package com.github.jcunit.utils;

import com.github.jcunit.core.tuples.Tuple;

import java.io.PrintStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;

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
  
  
  public static String className(Class klass) {
    return className(klass, "");
  }
  
  private static String className(Class klass, String work) {
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

  @SuppressWarnings("unchecked")
  public static <T> List<T> concat(List<T> var, List<T> vars) {
    return Stream.concat(var.stream(), vars.stream()).collect(toList());
  }

  public static <E> Collection<E> intersection(Collection<E> a, Collection<E> b) {
    if (b.size() < a.size()) {
      Collection<E> x = b;
      b = a;
      a = x;
    }
    Collection<E> ret = new HashSet<>(a);
    for (E each : a) {
      if (b.contains(a))
        ret.add(each);
    }
    return ret;
  }
}
