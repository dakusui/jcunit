package com.github.dakusui.jcunitx.tests.validation.testclassesundertest;

import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.runners.junit4.JCUnit8;
import com.github.dakusui.jcunitx.runners.junit4.annotations.ParameterSource;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class NoTestMethod {
  @ParameterSource
  public SimpleParameter.Descriptor<String> simple() {
    return SimpleParameter.Descriptor.of(asList("1", "2"));
  }
}
