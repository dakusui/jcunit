package com.github.dakusui.jcunit.tests.features.api;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import com.github.dakusui.jcunit.runners.standard.annotations.Condition;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Given;
import org.junit.Test;
import org.junit.runner.Result;

import java.util.Iterator;
import java.util.List;

import static com.github.dakusui.jcunit.core.utils.Utils.asList;
import static org.junit.Assert.assertEquals;

public class TypedTestSuiteTest {
  public static class TestClass {
    @FactorField(intLevels = { 0, 1 })
    public int a;
    @FactorField(intLevels = { 0, 1 })
    public int b;
    @FactorField(intLevels = { 0, 1 })
    public int c;

    @Override
    public int hashCode() {
      return this.a;
    }

    @Override
    public boolean equals(Object anotherObject) {
      if (anotherObject instanceof TestClass) {
        TestClass another = (TestClass) anotherObject;
        return this.a == another.a && this.b == another.b && this.c == another.c;
      }
      return false;
    }

    public static TestClass create(int a, int b, int c) {
      TestClass ret = new TestClass();
      ret.a = a;
      ret.b = b;
      ret.c = c;
      return ret;
    }

    @Condition
    public boolean aIsZero() {
      return this.a == 0;
    }

    @Test
    public void test() {
      System.out.printf("Hello:%s,%s,%s%n", this.a, this.b, this.c);
    }

    @Test
    @Given("aIsZero")
    public void notQuadratic() {
      System.out.printf("Not a quadratic equation: %s, %s, %s%n", this.a, this.b, this.c);
    }
  }

  @Test
  public void givenSimpleTestClass$whenCreateTypedTestSuite$thenTupleCreatedCorrectly() {
    List<TestCase> testSuite = TestSuite.Typed.generate(TestClass.class);
    assertEquals("{a=0, b=0, c=0}", testSuite.get(0).getTuple().toString());
    assertEquals("{a=0, b=1, c=1}", testSuite.get(1).getTuple().toString());
    assertEquals("{a=1, b=0, c=1}", testSuite.get(2).getTuple().toString());
    assertEquals("{a=1, b=1, c=0}", testSuite.get(3).getTuple().toString());
    assertEquals(4, testSuite.size());
  }

  @Test
  public void givenSimpleTestClass$whenCreateTypedTestSuite$thenObjectCreatedCorrectly() {
    TestSuite.Typed<TestClass> testSuite = TestSuite.Typed.generate(TestClass.class);
    assertEquals(TestClass.create(0, 0, 0), testSuite.inject(0));
    assertEquals(TestClass.create(0, 1, 1), testSuite.inject(1));
    assertEquals(TestClass.create(1, 0, 1), testSuite.inject(2));
    assertEquals(TestClass.create(1, 1, 0), testSuite.inject(3));
    assertEquals(4, testSuite.size());
  }

  @Test
  public void givenSimpleTestClass$whenCreateTypedTestSuite$thenFactorSpaceLooksCorrect() {
    Iterator<String> expectations = asList(
        "a:0", "a:1",
        "b:0", "b:1",
        "c:0", "c:1"
    ).iterator();
    for (Factor eachFactor : TestSuite.Typed.generate(TestClass.class).getFactorSpace().factors) {
      for (Object eachLevel : eachFactor.levels) {
        assertEquals(expectations.next(), eachFactor.name + ":" + eachLevel);
      }
    }
  }

  @Test
  public void givenSimpleTestClass$whenCreateTypedTestSuite$thenTestCaseCanBeRun() {
    Result result = TestSuite.Typed.generate(TestClass.class).execute(0);
    assertEquals(2, result.getRunCount());
    assertEquals(0, result.getFailureCount());
  }

  @Test
  public void givenSimpleTestClass$whenCreateTypedTestSuite$thenTestCaseCanBeRunThroughOracles() {
    int count = 0;
    TestSuite.Typed<TestClass> testSuite =TestSuite.Typed.generate(TestClass.class);
    for (Utils.Consumer<TestClass> each : testSuite.getOracles(0)) {
      each.accept(testSuite.inject(0));
      count++;
    }
    assertEquals(2, count);
  }

  @Test
  public void givenSimpleTestClass$whenCreateTypedTestSuite$thenModelClassCorrect() {
    assertEquals(TestClass.class, TestSuite.Typed.generate(TestClass.class).getModelClass());
  }
}
