package com.github.dakusui.jcunit.tests.generators;

import com.github.dakusui.jcunit.annotations.*;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.generators.RandomTupleGenerator;
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
      generator = @Generator(value = RandomTupleGenerator.class, params = { @Param("100"), @Param("1") }),
      constraint = @Constraint(TestClass.CM.class))
  public static class TestClass1 extends TestClass {
  }

  @Test
  public void test1() {
    Result result = JUnitCore.runClasses(TestClass1.class);
    assertTrue(result.wasSuccessful());
    assertEquals(100, result.getRunCount());
  }


  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(value = RandomTupleGenerator.class, params = { @Param("-1"), @Param("1") }),
      constraint = @Constraint(TestClass.CM.class))
  public static class TestClass2 extends TestClass {
  }

  @Test(expected = InvalidTestException.class)
  public void test2() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass2.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(value = RandomTupleGenerator.class, params = { @Param("INVALID"), @Param("1") }),
      constraint = @Constraint(TestClass.CM.class))
  public static class TestClass3 extends TestClass {
  }

  @Test(expected = InvalidTestException.class)
  public void test3() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass3.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(value = RandomTupleGenerator.class, params = { @Param("100"), @Param("XYZ") }),
      constraint = @Constraint(TestClass.CM.class))
  public static class TestClass4 extends TestClass {
  }

  @Test(expected = InvalidTestException.class)
  public void test4() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass4.class);
    assertFalse(result.wasSuccessful());
    assertEquals(1, result.getFailureCount());
    throw result.getFailures().get(0).getException();
  }

  @RunWith(JCUnit.class)
  @TupleGeneration(
      generator = @Generator(value = RandomTupleGenerator.class, params = { @Param("123"), @Param("SYSTEM_PROPERTY") }),
      constraint = @Constraint(TestClass.CM.class))
  public static class TestClass5 extends TestClass {
  }

  @Test
  public void test5() throws Throwable {
    Result result = JUnitCore.runClasses(TestClass5.class);
    assertTrue(result.wasSuccessful());
    assertEquals(123, result.getRunCount());
  }
}
