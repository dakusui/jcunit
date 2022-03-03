package com.github.dakusui.jcunitx.runners.helpers;

import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.metamodel.parameters.RegexParameter;
import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;

import static java.util.Arrays.asList;

public enum ParameterUtils {
  ;

  @SafeVarargs
  public static <T> SimpleParameter.Descriptor<T> simple(T... values) {
    return SimpleParameter.Descriptor.of(asList(values));
  }

  public static RegexParameter.Descriptor regex(String regex) {
    return RegexParameter.Descriptor.of(regex);
  }

}