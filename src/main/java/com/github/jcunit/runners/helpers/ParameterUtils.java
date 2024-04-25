package com.github.jcunit.runners.helpers;

import com.github.jcunit.factorspace.Parameter;

import java.util.function.Function;

import static java.util.Arrays.asList;

public enum ParameterUtils {
  ;

  @SafeVarargs
  public static <T> Parameter.Simple.Factory<T> simple(T... values) {
    return Parameter.Simple.Factory.of(asList(values));
  }

  public static <T> Parameter.Regex.Factory<T> regex(String regex, Function<String, T> function) {
    return Parameter.Regex.Factory.of(regex, function);
  }

  public static Parameter.Regex.Factory<String> regex(String regex) {
    return regex(regex, Function.identity());
  }
}