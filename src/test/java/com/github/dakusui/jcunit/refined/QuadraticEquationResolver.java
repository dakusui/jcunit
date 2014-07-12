package com.github.dakusui.jcunit.refined;

public class QuadraticEquationResolver {
  private final double a;
  private final double b;
  private final double c;

  public static class Solutions {
    final double x1;
    final double x2;

    public Solutions(double x1, double x2) {
      this.x1 = x1;
      this.x2 = x2;
    }

    public String toString() {
      return String.format("(%f,%f)", x1, x2);
    }
  }

  public QuadraticEquationResolver(double a, double b, double c) {
    this.a = a;
    this.b = b;
    this.c = c;
  }

  public Solutions resolve() {
    return new Solutions(
        (-b + Math.sqrt(b * b - 4 * c * a)) / (2 * a),
        (-b - Math.sqrt(b * b - 4 * c * a)) / (2 * a)
    );
  }

  public static  void main(String... args) {
    System.out.println(new QuadraticEquationResolver(1, -2, 1).resolve());
  }
}
