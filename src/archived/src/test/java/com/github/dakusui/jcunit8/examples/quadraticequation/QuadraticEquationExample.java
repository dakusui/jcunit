package com.github.dakusui.jcunit8.examples.quadraticequation;

import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.JUnit4_13Workaround;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Given;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.*;
import org.junit.runner.RunWith;
import org.mockito.internal.matchers.LessThan;

import static java.util.Arrays.asList;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * This is an "example" class, intended to be executed by a "real" test class.
 */
@SuppressWarnings("NewClassNamingConvention")
@RunWith(JCUnit8.class)
public class QuadraticEquationExample extends JUnit4_13Workaround {
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

  @BeforeClass
  public static void beforeClass() {
    System.out.println("beforeClass");
  }

  @Before
  public void before() {
    System.out.println("before");
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

  @After
  public void after() {
    System.out.println("after");
  }

  @AfterClass
  public static void afterClass() {
    System.out.println("afterClass");
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
