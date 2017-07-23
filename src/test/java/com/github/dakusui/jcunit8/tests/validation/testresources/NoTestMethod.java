package com.github.dakusui.jcunit8.tests.validation.testresources;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class NoTestMethod {
  @ParameterSource
  public Parameter.Simple.Factory<Integer> a() {
    return Parameter.Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

}
