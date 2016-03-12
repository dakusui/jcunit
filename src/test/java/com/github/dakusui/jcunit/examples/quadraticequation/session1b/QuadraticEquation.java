package com.github.dakusui.jcunit.examples.quadraticequation.session1b;

/**
 * Quadratic equation solver.
 *
 * <ul>
 *   <li>session 1: Initial version.</li>
 * </ul>
 */
public class QuadraticEquation {
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

  public QuadraticEquation(int a, int b, int c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Solutions solve() {
    double a = this.a;
    double b = this.b;
    double c = this.c;
    return new Solutions(
        (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a),
        (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a)
    );
  }
}
