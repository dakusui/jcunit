package com.github.dakusui.jcunitx.examples.quadratic;

/**
 * An example SUT.
 */
public class QuadraticEquationSolver {
  public static double solve1(int a, int b, int c) {
    System.out.printf("a=%d, b=%d, c=%d%n", a, b, c);
    return (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a);
  }

  public static double solve2(int a, int b, int c) {
    System.out.printf("a=%d, b=%d, c=%d%n", a, b, c);
    return (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a);
  }
}
