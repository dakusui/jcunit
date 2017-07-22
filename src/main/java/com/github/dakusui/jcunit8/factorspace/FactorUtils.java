package com.github.dakusui.jcunit8.factorspace;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

public enum FactorUtils {
  ;

  public static List<String> toFactorNames(Collection<Factor> factors) {
    return factors.stream().map(Factor::getName).collect(Collectors.toList());
  }
}
