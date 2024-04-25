package com.github.dakusui.jcunit8.sandbox.core;

import java.math.BigDecimal;
import java.util.Arrays;

public interface FactorFactory<T> {
  Factor<T> create(Class<?> testClass, Object... args);
  
  class ParsingAsInt implements FactorFactory<Integer> {
    @Override
    public Factor<Integer> create(Class<?> testClass, Object... args) {
      return Factor.create(Arrays.stream(args).map(v -> (String) v).map(Integer::parseInt).toArray());
    }
  }
  
  class ParsingAsDecimal implements FactorFactory<BigDecimal> {
    @Override
    public Factor<BigDecimal> create(Class<?> testClass, Object... args) {
      return Factor.create(Arrays.stream(args).map(v -> (String) v).map(BigDecimal::new).toArray());
    }
  }
  
  class StringLevels implements FactorFactory<String> {
    @Override
    public Factor<String> create(Class<?> testClass, Object... args) {
      return null;
    }
  }
  
  class MethodNames implements FactorFactory<Object> {
    @Override
    public Factor<Object> create(Class<?> testClass, Object... args) {
      return null;
    }
  }
}
