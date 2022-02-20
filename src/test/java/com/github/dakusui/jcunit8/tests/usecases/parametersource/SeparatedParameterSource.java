package com.github.dakusui.jcunit8.tests.usecases.parametersource;

import com.github.dakusui.jcunit8.metamodel.parameters.Simple;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;

import static java.util.Arrays.asList;

public class SeparatedParameterSource {
  @ParameterSource
  public Simple.Factory<Integer> a() {
    return Simple.Factory.of(asList(1, 2, 3));
  }
}
