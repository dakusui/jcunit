package com.github.dakusui.jcunitx.tests.usecases.parametersource;

import com.github.dakusui.jcunitx.metamodel.parameters.Simple;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;

import static java.util.Arrays.asList;

public class SeparatedParameterSource {
  @ParameterSource
  public Simple.Factory<Integer> a() {
    return Simple.Factory.of(asList(1, 2, 3));
  }
}
