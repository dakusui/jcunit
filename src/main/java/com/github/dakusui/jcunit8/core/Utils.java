package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;

import static java.util.Arrays.asList;

public enum Utils {
  ;

  public static <T> List<T> unique(List<T> in) {
    return new ArrayList<>(new LinkedHashSet<>(in));
  }

  public static Factor.Internal createInternalFactor(final String name, final Object[] args) {
    return new Factor.Internal() {
      @Override
      public String getName() {
        return name;
      }

      @Override
      public List<Object> getLevels() {
        return asList(args);
      }
    };
  }

  public static Tuple project(List<String> keys, Tuple from) {
    Tuple.Builder builder = new Tuple.Builder();
    keys.forEach((String key) -> builder.put(key, from.get(key)));
    return builder.build();
  }
}
