package com.github.dakusui.jcunit8.runners.helpers;

import com.github.dakusui.jcunit8.factorspace.Parameter;

import java.util.function.Function;

import static java.util.Arrays.asList;

public enum ParameterUtils {
  ;

  @SafeVarargs
  public static <T> Parameter.Simple.Factory<T> simple(T... values) {
    return Parameter.Simple.Factory.of(asList(values));
  }

  public static Parameter.Regex.Factory<String> regex(String regex) {
    return Parameter.Regex.Factory.of(regex);
  }
}