package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit8.model.factorspace.Factor;

import java.util.List;

import static java.util.Arrays.asList;

public enum Utils {
  ;

  public static Factor createFactor(final String name, final Object[] args) {
    return new Factor() {
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
