package com.github.dakusui.jcunit8.examples.quadraticequation;

import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

@RunWith(JCUnit8.class)
public class QuadraticEquationExample {
  @ParameterSource
  public Simple.Factory<Integer> a() {
    return Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @ParameterSource
  public Simple.Factory<Integer> b() {
    return Simple.Factory.of(asList(-1, 0, 1, 2, 4, 8));
  }

  @ParameterSource
  public Simple.Factory<Integer> c() {
    return Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @Condition(constraint = true)
  public boolean discriminantIsNonNegative(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    return b * b - 4 * c * a >= 0;
  }

  @Condition(constraint = true)
  public boolean aIsNonZero(@From("a") int a) {
    return a != 0;
  }

  @SuppressWarnings("unused")
  @Test
  @Given("discriminantIsNonNegative")
  public void performScenario1(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    double x1 = sut1(a, b, c);
    assertThat(Math.abs(a * x1 * x1 + b * x1 + c), new LessThan<>(0.01));
  }

  @SuppressWarnings("unused")
  @Test
  @Given("discriminantIsNonNegative")
  public void performScenario2(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    double x2 = sut2(a, b, c);
    assertThat(Math.abs(a * x2 * x2 + b * x2 + c), new LessThan<>(0.01));
  }


  private double sut1(int a, int b, int c) {
    System.out.printf("a=%d, b=%d, c=%d%n", a, b, c);
    return (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a);
  }

  private double sut2(int a, int b, int c) {
    System.out.printf("a=%d, b=%d, c=%d%n", a, b, c);
    return (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a);
  }
}
