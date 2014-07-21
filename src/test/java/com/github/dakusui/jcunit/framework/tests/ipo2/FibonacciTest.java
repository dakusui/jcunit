package com.github.dakusui.jcunit.framework.tests.ipo2;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.assertEquals;

@RunWith(Parameterized.class)
public class FibonacciTest {
  public static class Fibonacci {
    public static int compute(int i) {
      if (i == 0) return 0;
      if (i == 1) return 1;
      return compute(i - 1) + compute(i - 2);
    }
  }
  @Parameterized.Parameters
  public static Collection<Object[]> data() {
    return Arrays.asList(new Object[][] {
        { 0, 0 }, { 1, 1 }, { 2, 1 }, { 3, 2 }, { 4, 3 }, { 5, 5 }, { 6, 8 }
    });
  }

  private int fInput;

  private int fExpected;

  public FibonacciTest(int input, int expected) {
    fInput= input;
    fExpected= expected;
  }

  @Test
  public void test() {
    assertEquals(fExpected, Fibonacci.compute(fInput));
  }
}
