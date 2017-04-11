package com.github.dakusui.jcunit.examples.quadraticequation.session4;

/**
 * Quadratic equation solver.
 *
 * <ul>
 *   <li>session 1: Initial version.</li>
 *   <li>session 4: Implement parameter safeCheck in solve method.</li>
 * </ul>
 */
public class QuadraticEquationSolver {
  private final int a;
  private final int b;
  private final int c;

  public static class Solutions {
    public final double x1;
    public final double x2;

    public Solutions(double x1, double x2) {
      this.x1 = x1;
      this.x2 = x2;
    }

    public String toString() {
      return String.format("(%f,%f)", x1, x2);
    }
  }

  public QuadraticEquationSolver(int a, int b, int c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Solutions solve() {
    if (Math.abs(a) > 100 || Math.abs(b) > 100  || Math.abs(c) > 100 ) throw new IllegalArgumentException(String.format("Coefficient(s) too large. (a, b, c)=(%f, %f, %f)", a, b, c));
    if (a == 0) throw new IllegalArgumentException("Not a quadratic equation.");
    if (b * b - 4 * c * a < 0) throw new IllegalArgumentException("No real solutions.");
    double a = this.a;
    double b = this.b;
    double c = this.c;
    return new Solutions(
        (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a),
        (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a)
    );
  }
}
