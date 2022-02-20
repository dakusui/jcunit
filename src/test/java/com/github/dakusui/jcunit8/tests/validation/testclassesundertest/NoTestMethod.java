package com.github.dakusui.jcunit8.tests.validation.testclassesundertest;

import com.github.dakusui.jcunit8.metamodel.parameters.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class NoTestMethod {
  @ParameterSource
  public Simple.Factory<String> simple() {
    return Simple.Factory.of(asList("1", "2"));
  }
}
