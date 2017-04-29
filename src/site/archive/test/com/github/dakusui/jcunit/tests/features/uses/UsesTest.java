package com.github.dakusui.jcunit.tests.features.uses;

import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.annotations.Uses;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;

public class UsesTest {

  @RunWith(JCUnit.class)
  public static class TestA {
    @FactorField public int a;
    @FactorField public int b;
    @FactorField public int c;
    @Test
    @Uses("a")
    public void testA() {
      System.out.println("a=" + this.a);
    }
    public static void runTests() {
      Result result = JUnitCore.runClasses(TestA.class);
      assertEquals(7, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(46, result.getIgnoreCount());
    }
  }
  @Test
  public void testTestA() {
    TestA.runTests();
  }

  @RunWith(JCUnit.class)
  public static class TestBC {
    @FactorField
    public int a;
    @FactorField
    public int b;
    @FactorField
    public int c;

    @Test
    @Uses({ "b", "c" })
    public void testBC() {
      System.out.println("a=" + this.a + ":b=" + b);
    }
    public static void runTests() {
      Result result = JUnitCore.runClasses(TestBC.class);
      assertEquals(49, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(4, result.getIgnoreCount());
    }
  }
  @Test
  public void testTestBC() {
    TestBC.runTests();
  }

  @RunWith(JCUnit.class)
  public static class TestONE {
    @FactorField
    public int a;
    @FactorField
    public int b;
    @FactorField
    public int c;

    @Test
    @Uses({})
    public void testONE() {
      UTUtils.stdout().println("a=" + this.a + ":b=" + b + ":c=" + c);
    }
    public static void runTests() {
      Result result = JUnitCore.runClasses(TestONE.class);
      assertEquals(1, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(52, result.getIgnoreCount());
    }
  }
  @Test
  public void testTestONE() {
    TestONE.runTests();
  }

  @RunWith(JCUnit.class)
  public static class TestALL {
    @FactorField
    public int a;
    @FactorField
    public int b;
    @FactorField
    public int c;

    @Test
    @Uses()
    public void testALL() {
      UTUtils.stdout().println("a=" + this.a + ":b=" + b + ":c=" + c);
    }
    public static void runTests() {
      Result result = JUnitCore.runClasses(TestALL.class);
      assertEquals(53, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(0, result.getIgnoreCount());
    }
  }
  @Test
  public void testTestALL() {
    TestALL.runTests();
  }

  @RunWith(JCUnit.class)
  public static class TestTwoMethods {
    @FactorField
    public int a;
    @FactorField
    public int b;
    @FactorField
    public int c;

    @Test
    @Uses({"c", "a"})
    public void testCA() {
      UTUtils.stdout().println("a=" + this.a + ":b=" + b + ":c=" + c);
    }

    @Test
    @Uses({"a", "b"})
    public void testAB() {
      UTUtils.stdout().println("a=" + this.a + ":b=" + b + ":c=" + c);
    }

    public static void runTests() {
      Result result = JUnitCore.runClasses(TestTwoMethods.class);
      assertEquals(98, result.getRunCount());
      assertEquals(0, result.getFailureCount());
      assertEquals(1, result.getIgnoreCount());
    }
  }
  @Test
  public void testTestTwoMethods() {
    TestTwoMethods.runTests();
  }

}
