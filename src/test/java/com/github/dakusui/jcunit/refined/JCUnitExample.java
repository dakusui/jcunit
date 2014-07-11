package com.github.dakusui.jcunit.refined;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.factor.FactorField;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class JCUnitExample {
  @FactorField
  public int f1;
  @FactorField
  public long f2;
  @FactorField
  public String f3;

  @Test
  public void test() {
    System.out.println(String.format("f1=%d, f2=%d, f3=%s", f1, f2, f3));
  }
}
