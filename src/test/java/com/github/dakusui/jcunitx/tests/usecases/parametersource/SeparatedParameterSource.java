package com.github.dakusui.jcunitx.tests.usecases.parametersource;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;

import static java.util.Arrays.asList;

public class SeparatedParameterSource {
  @ParameterSource
  public SimpleParameter.Descriptor<Integer> a() {
    return SimpleParameter.Descriptor.of(asList(1, 2, 3));
  }
}
