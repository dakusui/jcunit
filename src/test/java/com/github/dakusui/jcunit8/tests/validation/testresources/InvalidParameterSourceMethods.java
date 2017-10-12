package com.github.dakusui.jcunit8.tests.validation.testresources;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
public class InvalidParameterSourceMethods extends InvalidTestClass {
  @SuppressWarnings("unused")
  @ParameterSource
  public Parameter.Simple.Factory<Integer> a(int a) {
    return Parameter.Simple.Factory.of(asList(-1, 0, 1, 2, a));
  }

  @SuppressWarnings("unused")
  @ParameterSource
  private Parameter.Simple.Factory<Integer> b() {
    return Parameter.Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @SuppressWarnings("unused")
  @ParameterSource
  public static Parameter.Simple.Factory<Integer> c() {
    return Parameter.Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @SuppressWarnings("unused")
  @Test
  public void test(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
  }
}
