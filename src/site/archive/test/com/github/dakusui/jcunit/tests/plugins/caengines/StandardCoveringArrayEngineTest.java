package com.github.dakusui.jcunit.tests.plugins.caengines;

import com.github.dakusui.jcunit.plugins.caengines.StandardCoveringArrayEngine;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.GenerateCoveringArrayWith;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class StandardCoveringArrayEngineTest {
  @RunWith(JCUnit.class)
  public static class Test1 {
    @FactorField
    public int i;

    @Test
    public void test() {
    }
  }

  @RunWith(JCUnit.class)
  public static class Test2 {
    @FactorField
    public int i;
    @FactorField
    public int j;

    @Test
    public void test() {
    }
  }

  @RunWith(JCUnit.class)
  @GenerateCoveringArrayWith(
      engine = @Generator(value = StandardCoveringArrayEngine.class, args = @Value("1"))
  )
  public static class Test3 {
    @FactorField
    public int i;
    @FactorField
    public int j;

    @Test
    public void test() {
    }
  }

  @RunWith(JCUnit.class)
  public static class Test4 {
    @FactorField
    public int i;
    @FactorField
    public int j;
    @FactorField
    public int k;

    @Test
    public void test() {
    }
  }

  @Test
  public void test1() {
    Result result = JUnitCore.runClasses(Test1.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(7, result.getRunCount());
  }

  @Test
  public void test2() {
    Result result = JUnitCore.runClasses(Test2.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(49, result.getRunCount());
  }

  @Test
  public void test3() {
    Result result = JUnitCore.runClasses(Test3.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(13, result.getRunCount());
  }

  @Test
  public void test4() {
    Result result = JUnitCore.runClasses(Test4.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(53, result.getRunCount());
  }
}
