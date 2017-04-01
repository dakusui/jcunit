package com.github.dakusui.jcunit8.examples;

import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.*;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;

@RunWith(JCUnit8.class)
@ConfigureWith(QuadraticEquationExample.QuadraticEquationConfigFactory.class)
public class QuadraticEquationExample {
  public static class QuadraticEquationConfigFactory extends ConfigureWith.ConfigFactory.Impl {
    @ParameterSource
    public static Simple.Factory<Integer> a() {
      return Simple.Factory.of(asList(1, 2, 4));
    }

    @ParameterSource
    public static Simple.Factory<Integer> b() {
      return Simple.Factory.of(asList(1, 2, 4, 8));
    }

    @ParameterSource
    public static Simple.Factory<Integer> c() {
      return Simple.Factory.of(asList(1, 2, 4));
    }

    @Condition(constraint = true)
    public static boolean discriminantIsNonNegative(
        @From("a") int a,
        @From("b") int b,
        @From("c") int c
    ) {
      return b * b - 4 * c * a >= 0;
    }

  }

  @SuppressWarnings("unused")
  @Oracle
  @Given("discriminantIsNonNegative")
  public void performScenario(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
  }

}
