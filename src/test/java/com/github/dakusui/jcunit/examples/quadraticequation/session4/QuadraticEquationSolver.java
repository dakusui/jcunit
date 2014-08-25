package com.github.dakusui.jcunit.examples.quadraticequation.session4;

public class QuadraticEquationSolver {
  private final double a;
  private final double b;
  private final double c;

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

  public QuadraticEquationSolver(double a, double b, double c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Solutions solve() {
    if (Math.abs(a) > 100 || Math.abs(b) > 100  || Math.abs(c) > 100 ) throw new IllegalArgumentException("Coefficient(s) too large.");
    if (a == 0) throw new IllegalArgumentException("Not a quadratic equation.");
    if (b * b - 4 * c * a < 0) throw new IllegalArgumentException("No real solutions.");
    return new Solutions(
        (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a),
        (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a)
    );
  }
}
