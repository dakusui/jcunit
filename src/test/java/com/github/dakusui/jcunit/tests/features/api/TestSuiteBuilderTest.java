package com.github.dakusui.jcunit.tests.features.api;

import com.github.dakusui.jcunit.core.TestSuite;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.runners.core.TestCase;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestSuiteBuilderTest {
  @Test
  public void test() {
    List<TestCase> testSuite = new TestSuite.Builder()
        .addFactor("factor1", Arrays.asList(1, 2, 3))
        .addConstraint(new Utils.Predicate<Tuple>() {
          @Override
          public boolean apply(Tuple in) {
            return !in.get("factor1").equals(1);
          }
        })
        .enableNegativeTests()
        .build();
    assertEquals(3, testSuite.size());
    assertEquals(2, testSuite.get(0).getTuple().get("factor1"));
    assertEquals(3, testSuite.get(1).getTuple().get("factor1"));
    assertEquals(1, testSuite.get(2).getTuple().get("factor1"));
    assertEquals(TestCase.Type.REGULAR, testSuite.get(0).getType());
    assertEquals(TestCase.Type.REGULAR, testSuite.get(1).getType());
    assertEquals(TestCase.Type.VIOLATION, testSuite.get(2).getType());
    assertEquals(0, testSuite.get(0).getId());
    assertEquals(1, testSuite.get(1).getId());
    assertEquals(2, testSuite.get(2).getId());
  }

  @Test
  public void test2() {
    List<TestCase> testSuite = new TestSuite.Builder()
        .addFactor("factor1", Arrays.asList(1, 2, 3))
        .addConstraint(new Utils.Predicate<Tuple>() {
          @Override
          public boolean apply(Tuple in) {
            return !in.get("factor1").equals(1);
          }
        })
        .disableNegativeTests()
        .build();
    assertEquals(2, testSuite.size());
    assertEquals(2, testSuite.get(0).getTuple().get("factor1"));
    assertEquals(3, testSuite.get(1).getTuple().get("factor1"));
    assertEquals(TestCase.Type.REGULAR, testSuite.get(0).getType());
    assertEquals(TestCase.Type.REGULAR, testSuite.get(1).getType());
    assertEquals(0, testSuite.get(0).getId());
    assertEquals(1, testSuite.get(1).getId());
  }

  @Test
  public void test3() {
    List<TestCase> testSuite = new TestSuite.Builder()
        .addConstraint(new Utils.Predicate<Tuple>() {
          @Override
          public boolean apply(Tuple in) {
            return !in.get("factor1").equals(1);
          }
        })
        .disableNegativeTests()
        .addBooleanFactor("boolean")
        .addByteFactor("byte")
        .addCharFactor("char")
        .addShortFactor("short")
        .addIntFactor("int")
        .addLongFactor("long")
        .addFloatFactor("float")
        .addDoubleFactor("double")
        .addStringFactor("string")
        .addEnumLevels("enum", TestCase.Type.class)
        .build();
    assertEquals(93, testSuite.size());
  }

}
