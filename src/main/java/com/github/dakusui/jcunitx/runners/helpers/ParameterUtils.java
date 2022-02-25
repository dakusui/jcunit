package com.github.dakusui.jcunitx.runners.helpers;

import com.github.dakusui.jcunitx.metamodel.parameters.Regex;
import com.github.dakusui.jcunitx.metamodel.parameters.Simple;

import static java.util.Arrays.asList;

public enum ParameterUtils {
  ;

  @SafeVarargs
  public static <T> Simple.Factory<T> simple(T... values) {
    return Simple.Factory.of(asList(values));
  }

  public static Regex.Factory regex(String regex) {
    return Regex.Factory.of(regex);
  }
}