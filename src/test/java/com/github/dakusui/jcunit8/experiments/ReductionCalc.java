package com.github.dakusui.jcunit8.experiments;

import org.junit.Test;

import static java.lang.Math.pow;

public class ReductionCalc {
  private static final int[][] T$INC  = {
      { 44, 52 },
      { 47, 57 },
      { 50, 58 },
  };
  private static final int[][] T$JOIN = {
      { 57, 81 },
      { 60, 87 },
      { 64, 96 },
  };
  private static final double  DELTA  = 0.1;
  private static final int     MIN_R  = 0;
  private static final int     MAX_R  = 2;


  private int sizeOfT$INC(int i, int n) {
    return T$INC[n - 3][i - 1];
  }

  private int sizeOfT$JOIN(int i, int n) {
    return T$JOIN[n - 3][i - 1];
  }

  @Test
  public void test() {
    System.out.println("inc(1, 3)=" + sizeOfT$INC(1, 3));
    System.out.println("inc(2, 5)=" + sizeOfT$INC(2, 5));
    System.out.println("join(1, 3)=" + sizeOfT$JOIN(1, 3));
    System.out.println("join(2, 5)=" + sizeOfT$JOIN(2, 5));
  }

  @Test
  public void gain1_3() {
    System.out.println("\\addplot[color=red,mark=o] coordinates {");
    for (double i = MIN_R; i <= MAX_R; i += DELTA) {
      double r = pow(10.0, i);
      System.out.printf("  (%s,%s)%n", r, gain(r, 1, 3));
    }
    System.out.println("};");
    System.out.println("\\addlegendentry{\"gain(r, 1, 3)\"}");
  }

  @Test
  public void gain1_4() {
    System.out.println("\\addplot[color=green,mark=x] coordinates {");
    for (double i = MIN_R; i <= MAX_R; i += DELTA) {
      double r = pow(10.0, i);
      System.out.printf("(%s,%s)%n", r, gain(r, 1, 4));
    }
    System.out.println("};");
    System.out.println("n\\addlegendentry{\"gain(r, 1, 4)\"}");
  }

  @Test
  public void gain1_5() {
    System.out.println("\\addplot[color=blue,mark=+] coordinates {");
    for (double i = MIN_R; i <= MAX_R; i += DELTA) {
      double r = pow(10.0, i);
      System.out.printf("(%s,%s)%n", r, gain(r, 1, 5));
    }
    System.out.println("};");
    System.out.println("\\addlegendentry{\"gain(r, 1, 5)\"}");
  }

  @Test
  public void gain2_3() {
    System.out.println("\\addplot[color=red,mark=x] coordinates {");
    for (double i = MIN_R; i <= MAX_R; i += DELTA) {
      double r = pow(10.0, i);
      System.out.printf("(%s,%s)%n", r, gain(r, 2, 3));
    }
    System.out.println("};");
    System.out.println("\\addlegendentry{\"gain(r, 2, 3)\"}");
  }

  @Test
  public void gain2_4() {
    System.out.println("\\addplot[color=green,mark=-] coordinates {");
    for (double i = MIN_R; i <= MAX_R; i += DELTA) {
      double r = pow(10.0, i);
      System.out.printf("(%s,%s)%n", r, gain(r, 2, 4));
    }
    System.out.println("};");
    System.out.println("\\addlegendentry{\"gain(r, 2, 4)\"}");
  }

  @Test
  public void gain2_5() {
    System.out.println("\\addplot[color=blue,mark=+] coordinates {");
    for (double i = MIN_R; i <= MAX_R; i += DELTA) {
      double r = pow(10.0, i);
      System.out.printf("(%s,%s)%n", r, gain(r, 2, 5));
    }
    System.out.println("};");
    System.out.println("\\addlegendentry{\"gain(r, 2, 5)\"}");
  }

  public double gain(double r, int s, int n) {
    double p = 0;
    for (int i = 1; i <= s; i++) {
      p += C_t$JOIN(i, n, r);
    }
    double q = 0;
    for (int i = 1; i <= s; i++) {
      q += C_t$INC(i, n, r);
    }
    return 1.0 - p / q;
  }

  private double C_t$JOIN(int i, int n, double r) {
    return r * (i == 1 ? pow(n, i) * sizeOfT$JOIN(i, n) : 0)
        + pow(n, i) * sizeOfT$JOIN(i, n);
  }

  private double C_t$INC(int i, int n, double r) {
    return (pow(n, i) * r + 1.0) * sizeOfT$INC(i, n);
  }
}
