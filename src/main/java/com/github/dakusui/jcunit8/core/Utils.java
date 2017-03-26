package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

public enum Utils {
  ;

  public static <T> List<T> unique(List<T> in) {
    return new ArrayList<>(new LinkedHashSet<>(in));
  }

  public static Tuple project(List<String> keys, Tuple from) {
    Tuple.Builder builder = new Tuple.Builder();
    keys.forEach((String key) -> builder.put(key, from.get(key)));
    return builder.build();
  }
}
