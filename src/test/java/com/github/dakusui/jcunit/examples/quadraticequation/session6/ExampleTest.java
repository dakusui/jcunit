package com.github.dakusui.jcunit.examples.quadraticequation.session6;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

@RunWith(Parameterized.class)
public class ExampleTest {
  @Parameterized.Parameters
  public static Object[][] params() {
    return new Object[][]{
        {1, 2, 3},
        {1, 2, 3}
    };
  }

  public ExampleTest(int i, int j, int k) {

  }

  @Test
  public void test() {

  }
}
