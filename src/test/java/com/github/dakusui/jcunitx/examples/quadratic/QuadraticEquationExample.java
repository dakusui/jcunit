package com.github.dakusui.jcunitx.examples.quadratic;

import com.github.dakusui.jcunit8.models.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import com.github.dakusui.jcunitx.runners.annotations.Condition;
import com.github.dakusui.jcunitx.runners.annotations.Constraints;
import com.github.dakusui.jcunitx.runners.annotations.From;
import com.github.dakusui.jcunitx.runners.annotations.Given;
import com.github.dakusui.jcunitx.runners.junit5.JCUnitX;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;

import static com.github.dakusui.pcond.TestAssertions.assertThat;
import static com.github.dakusui.pcond.functions.Predicates.lessThan;
import static java.util.Arrays.asList;

@ExtendWith(JCUnitX.class)
@Constraints({ "aIsNonZero", "discriminantIsNonNegative" })
public class QuadraticEquationExample {
  @BeforeAll
  public static void beforeAll() {
    System.out.println("beforeClass");
  }

  @AfterAll
  public static void afterAll() {
    System.out.println("afterClass");
  }

  @ParameterSource
  public Simple.Factory<Integer> a() {
    return Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @ParameterSource
  public Simple.Factory<Integer> b() {
    return Simple.Factory.of(asList(-2, -1, 0, 1, 2, 4, 8));
  }

  @ParameterSource
  public Simple.Factory<Integer> c() {
    return Simple.Factory.of(asList(-1, 0, 1, 2, 4));
  }

  @Condition
  public boolean discriminantIsNonNegative(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c) {
    return b * b - 4 * c * a >= 0;
  }

  @Condition
  public boolean aIsNonZero(@From("a") int a) {
    return a != 0;
  }

  @BeforeEach
  public void before() {
    System.out.println("before");
  }

  @Test
  @Given("discriminantIsNonNegative")
  public void performScenario1(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    double x1 = QuadraticEquationSolver.solve1(a, b, c);
    assertThat(Math.abs(a * x1 * x1 + b * x1 + c), lessThan(0.01));
  }

  @Test
  @Given("discriminantIsNonNegative")
  public void performScenario2(
      @From("a") int a,
      @From("b") int b,
      @From("c") int c
  ) {
    double x2 = QuadraticEquationSolver.solve2(a, b, c);
    assertThat(Math.abs(a * x2 * x2 + b * x2 + c), lessThan(0.01));
  }

  @AfterEach
  public void after() {
    System.out.println("after");
  }

}
