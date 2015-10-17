package com.github.dakusui.jcunit.tests.factor;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.factor.TupleLevelsProvider;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class TupleLevelsProviderTest {
  public static class Struct {
    @FactorField(intLevels = { 1, 2 })
    public int f1;
    @FactorField(intLevels = { 3, 4 })
    public int f2;
  }

  @RunWith(JCUnit.class)
  public static class TestClass {
    @FactorField(levelsProvider = TupleLevelsProvider.class)
    public Struct struct;
    @FactorField(intLevels = { 5, 6 })
    public int    f;

    @Test
    public void test() {
      UTUtils.out.println(String.format("(f, f1, f2)=(%d, %d, %d)", f, struct.f1, struct.f2));
    }
  }

  @Test
  public void normalTest1() {
    Result result = JUnitCore.runClasses(TestClass.class);
    assertEquals(true, result.wasSuccessful());
    assertEquals(8, result.getRunCount());
  }

  @RunWith(JCUnit.class)
  public static class TestClass2 {
    @FactorField(levelsProvider = TupleLevelsProvider.class)
    public Struct struct;

    @Test
    public void test() {
      UTUtils.out.println(String.format("(f1, f2)=(%d, %d)", struct.f1, struct.f2));
    }
  }

  @Test(expected = InvalidTestException.class)
  public void negativeTest2() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass2.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(value = IPO2TupleGenerator.class, params = @Param("3"))
  )
  public static class TestClass3 {
    @FactorField
    public int f1;
    @FactorField
    public int f2;

    @Test
    public void test() {
      System.out.println(String.format("(f1, f2)=(%d, %d)", f1, f2));
    }
  }

  @Test(expected = InvalidTestException.class)
  public void negativeTest3() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass3.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }


  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(value = IPO2TupleGenerator.class, params = @Param("1"))
  )
  public static class TestClass4 {
    @FactorField
    public int f1;
    @FactorField
    public int f2;

    @Test
    public void test() {
      UTUtils.out.println(String.format("(f1, f2)=(%d, %d)", f1, f2));
    }
  }

  @Test(expected = InvalidTestException.class)
  public void negativeTest4() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass4.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }


  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(value = IPO2TupleGenerator.class, params = { @Param("2"), @Param("hello!") })
  )
  public static class TestClass5 {
    @FactorField
    public int f1;
    @FactorField
    public int f2;

    @Test
    public void test() {
      UTUtils.out.println(String.format("(f1, f2)=(%d, %d)", f1, f2));
    }
  }

  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

  @Test(expected = InvalidTestException.class)
  public void negativeTest5() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass5.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }
}
