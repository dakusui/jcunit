package com.github.dakusui.jcunit8.examples;

import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JCUnit8.class)
@ConfigureWith(QuadraticEquationExample.QuadraticEquationConfigFactory.class)
public class QuadraticEquationExample {
  public static class QuadraticEquationConfigFactory extends ConfigureWith.ConfigFactory.Impl {
    @ParameterSource
    public Simple.Factory<Integer> a() {
      return Simple.Factory.of(asList(0, -1, 1, 2, 4));
    }

    @ParameterSource
    public Simple.Factory<Integer> b() {
      return Simple.Factory.of(asList(0, -1, 1, 2, 4, 8));
    }

    @ParameterSource
    public Simple.Factory<Integer> c() {
      return Simple.Factory.of(asList(0, -1, 1, 2, 4));
    }

    @Condition(constraint = true)
    public boolean discriminantIsNonNegative(
        @From("a") int a,
        @From("b") int b,
        @From("c") int c
    ) {
      return print(print(b * b - 4 * c * a) >= 0);
    }

    static private <T> T print(T v) {
      System.out.println(v);
      return v;
    }
  }

  @SuppressWarnings("unused")
  @Test
  @Given("discriminantIsNonNegative")
  public void performScenario(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    double x1 = sut1(a, b, c);
    assertThat(a * x1 * x1 + b * x1 + c, equalTo(0.0));
  }


  private double sut1(int a, int b, int c) {
    return (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a);
  }

  private double sut2(int a, int b, int c) {
    return (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a);
  }
}
