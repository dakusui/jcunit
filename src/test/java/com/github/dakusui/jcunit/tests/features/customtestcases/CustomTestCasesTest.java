package com.github.dakusui.jcunit.tests.features.customtestcases;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import com.github.dakusui.jcunit.runners.standard.annotations.CustomTestCases;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.testutils.Metatest;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.LinkedList;
import java.util.List;

public class CustomTestCasesTest {
  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void testNormal() {
    new Normal().runTests();
  }

  @Test
  public void testNull() {
    new Null().runTests();
  }

  @Test
  public void testIncompatible() {
    new Incompatible().runTests();
  }

  @Test
  public void testNullInList() {
    new NullInList().runTests();
  }

  @RunWith(JCUnit.class)
  public static class Base extends Metatest {
    @FactorField(intLevels = { 999 })
    public int a;
    @FactorField(intLevels = { 999 })
    public int b;

    public Base(int expectedRunCount, int expectedFailureCount, int expectedIgnoreCount) {
      super(expectedRunCount, expectedFailureCount, expectedIgnoreCount);
    }

    @Test
    public void test() {
      UTUtils.stdout().println(this.a + ":" + this.b);
    }
  }

  public static class Normal extends Base {
    public Normal() {
      super(4, 0, 0);
    }

    @SuppressWarnings("unused")
    @CustomTestCases
    public static Iterable<Base> customObjectTestCases() {
      List<Base> ret = new LinkedList<Base>();
      ret.add(create(123, 111));
      return ret;
    }

    @SuppressWarnings("unused")
    @CustomTestCases
    public static Iterable<Tuple> customTupleTestCases() {
      List<Tuple> ret = new LinkedList<Tuple>();
      ret.add(TestCaseUtils.toTestCase(create(456, 222)));
      return ret;
    }

    @CustomTestCases
    public static Normal singleObjectCustomTestCase() {
      return create(789, 333);
    }

    private static Normal create(int a, int b) {
      Normal ret = new Normal();
      ret.a = a;
      ret.b = b;
      return ret;
    }
  }

  public static class Null extends Base {
    public Null() {
      super(1, 1, 0);
    }

    @CustomTestCases
    public static Tuple nullTestCase() {
      return null;
    }
  }

  public static class Incompatible extends Base {
    public Incompatible() {
      super(1, 1, 0);
    }

    @CustomTestCases
    public static Object incompatibleTestCase() {
      return new Object();
    }
  }

  public static class NullInList extends Base {
    public NullInList() {
      super(1, 1, 0);
    }

    @CustomTestCases
    public static List<Tuple> nullTestCaseInList() {
      LinkedList<Tuple> ret = new LinkedList<Tuple>();
      ret.add(null);
      return ret;
    }
  }
}
