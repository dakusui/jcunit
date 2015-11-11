package com.github.dakusui.jcunit.tests.caengines;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.caengines.RandomCoveringArrayEngine;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public class RandomCoveringArrayEngineTest {
  public static abstract class TestClass {
    public static class CM extends ConstraintChecker.Base {
      @Override
      public boolean check(Tuple tuple) throws UndefinedSymbol {
        return !tuple.get("f").equals(tuple.get("g"));
      }
    }

    @SuppressWarnings("unused")
    @FactorField
    public int f;
    @SuppressWarnings("unused")
    @FactorField
    public int g;
    @SuppressWarnings("unused")
    @FactorField
    public int h;

    @Test
    public void test() {
      assertNotEquals(f, g);
    }
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          /**
           * Statistically, it is possible that random covering engine generates less than 100
           * test cases because JCUnit deduplicate identical ones after suite generation.
          */
          value = RandomCoveringArrayEngine.class,
          configValues = {
              @Value("100")
          }),
      checker = @Checker(TestClass.CM.class))
  public static class TestClass1 extends TestClass {
    @SuppressWarnings("unused")
    @FactorField
    public int i;
    @SuppressWarnings("unused")
    @FactorField
    public int j;
    @SuppressWarnings("unused")
    @FactorField
    public int k;
    @SuppressWarnings("unused")
    @FactorField
    public int l;
    @SuppressWarnings("unused")
    @FactorField
    public int m;
    @SuppressWarnings("unused")
    @FactorField
    public int n;
  }

  @Test
  public void normally100TestCasesShouldBeGeneratee() {
    Result result = JUnitCore.runClasses(TestClass1.class);
    assertTrue(result.wasSuccessful());
    assertEquals(100, result.getRunCount());
  }


  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = RandomCoveringArrayEngine.class,
          configValues = {
              // Only non-negative value is accepted
              @Value("-1")
          }),
      checker = @Checker(TestClass.CM.class))
  public static class TestClass2 extends TestClass {
  }

  @Test(expected = InvalidTestException.class)
  public void negativeNumberShouldBeRejectedAsTestSuiteSize() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass2.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = RandomCoveringArrayEngine.class,
          configValues = {
              @Value("INVALID") // Intentionally broken argument.
          }),
      checker = @Checker(TestClass.CM.class))
  public static class TestClass3 extends TestClass {
  }

  /**
   * Make sure a root exception that prevented test instantiation is thrown and
   * not wrapped by JCUnitException. In this case NumberFormatException.
   */
  @Test(expected = NumberFormatException.class)
  public void invalidTestSuiteSizeShouldBeRejected() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass3.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @GenerateWith(
      generator = @Generator(
          value = RandomCoveringArrayEngine.class,
          configValues = {
              @Value("100"),
              @Value("999") // This parameter is unnecessary and should be rejected.
          }),
      checker = @Checker(TestClass.CM.class))
  public static class TestClass4 extends TestClass {
  }

  @Test(expected = InvalidTestException.class)
  public void extraParameterShouldBeRejected() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass4.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }
}
