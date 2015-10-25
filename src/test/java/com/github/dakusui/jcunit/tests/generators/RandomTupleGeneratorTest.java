package com.github.dakusui.jcunit.tests.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.plugins.generators.RandomTupleGenerator;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static junit.framework.TestCase.assertFalse;
import static org.junit.Assert.*;

public class RandomTupleGeneratorTest {
  public static abstract class TestClass {
    public static class CM extends ConstraintManagerBase {
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
  @TupleGeneration(
      generator = @Generator(
          value = RandomTupleGenerator.class,
          params = {
              @Value("100")
          }),
      constraint = @Constraint(TestClass.CM.class))
  public static class TestClass1 extends TestClass {
  }

  @Test
  public void normally100TestCasesShouldBeGeneratee() {
    Result result = JUnitCore.runClasses(TestClass1.class);
    assertTrue(result.wasSuccessful());
    assertEquals(100, result.getRunCount());
  }


  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(
          value = RandomTupleGenerator.class,
          params = {
              // Only non-negative value is accepted
              @Value("-1")
          }),
      constraint = @Constraint(TestClass.CM.class))
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
  @TupleGeneration(
      generator = @Generator(
          value = RandomTupleGenerator.class,
          params = {
              @Value("INVALID") // Intentionally broken argument.
          }),
      constraint = @Constraint(TestClass.CM.class))
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
  @TupleGeneration(
      generator = @Generator(
          value = RandomTupleGenerator.class,
          params = {
              @Value("100"),
              @Value("999") // This parameter is unnecessary and should be rejected.
          }),
      constraint = @Constraint(TestClass.CM.class))
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
