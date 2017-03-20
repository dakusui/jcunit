package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.List;

import static java.util.Arrays.asList;

public enum Utils {
  ;

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
}
