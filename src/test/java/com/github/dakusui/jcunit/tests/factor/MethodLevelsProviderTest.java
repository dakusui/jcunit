package com.github.dakusui.jcunit.tests.factor;

import com.github.dakusui.jcunit.annotations.FactorField;
import com.github.dakusui.jcunit.core.factor.LevelsProviderBase;
import com.github.dakusui.jcunit.core.factor.MethodLevelsProvider;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 */
public class MethodLevelsProviderTest {
  public static class TargetClass {
    @SuppressWarnings("unused")
    @FactorField
    public int targetField;

    @SuppressWarnings("unused")
    public static int[] targetField() {
      return new int[] { 1, 10, 256 };
    }
  }

  @Test
  public void test() throws NoSuchFieldException {
    LevelsProviderBase provider = new MethodLevelsProvider();
    provider.init(new Object[]{ ReflectionUtils.getField(TargetClass.class, "targetField")});
    assertEquals(3, provider.size());
    assertEquals(1, provider.get(0));
    assertEquals(10, provider.get(1));
    assertEquals(256, provider.get(2));
  }
}
