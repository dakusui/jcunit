package com.github.dakusui.jcunit8.testutils;

import com.github.dakusui.jcunit8.models.Parameter;
import com.github.dakusui.jcunit8.models.scenario.Scenario;

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

  public static Scenario.Factory scenario(String regex) {
    return new Scenario.Factory(regex);
  }
}