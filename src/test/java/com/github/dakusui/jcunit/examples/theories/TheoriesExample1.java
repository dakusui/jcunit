package com.github.dakusui.jcunit.examples.theories;


import com.github.dakusui.jcunit.runners.theories.TheoriesWithJCUnit;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.experimental.theories.DataPoints;
import org.junit.experimental.theories.Theory;
import org.junit.runner.RunWith;

@RunWith(TheoriesWithJCUnit.class)
public class TheoriesExample1 {
  @DataPoints("posInt")
  public static int[] positiveIntegers() {
    return new int[] {
        1, 2, 3
    };
  }

  @Theory
  public void test1(
      int a,
      int b,
      int c,
      int d
  ) throws Exception {
    UTUtils.stdout().printf("a=%s, b=%s, c=%d, d=%d%n", a, b, c, d);
  }
}
