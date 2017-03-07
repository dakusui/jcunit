package com.github.dakusui.jcunit.tests.features.api;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.framework.TestCase;
import com.github.dakusui.jcunit.framework.TestSuite;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class TestSuiteBuilderTest {
  @Test
  public void basicTestSuiteBuildingNegativeTestGenerationEnabled() {
    TestSuite testSuite = new TestSuite.Builder()
        .addFactor("factor1", 1, 2, 3)
        .addConstraint(new TestSuite.Predicate("factor1 mustn't be 1", "factor1") {
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
    assertEquals(TestCase.Category.REGULAR, testSuite.get(0).getCategory());
    assertEquals(TestCase.Category.REGULAR, testSuite.get(1).getCategory());
    assertEquals(TestCase.Category.VIOLATION, testSuite.get(2).getCategory());
    assertEquals(1, TestSuite.getViolatedConstraints(testSuite.get(2)).size());
    assertEquals("factor1 mustn't be 1", TestSuite.getViolatedConstraints(testSuite.get(2)).get(0).tag);
  }

  @Test
  public void basicTestSuiteBuildingNegativeTestGenerationDisabled() {
    TestSuite testSuite = new TestSuite.Builder()
        .addFactor("factor1", 1, 2, 3)
        .addConstraint(new TestSuite.Predicate("factor1 mustn't be 1", "factor1") {
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
    assertEquals(TestCase.Category.REGULAR, testSuite.get(0).getCategory());
    assertEquals(TestCase.Category.REGULAR, testSuite.get(1).getCategory());
  }

  @Test
  public void defaultLevelsCanBeUsed() {
    TestSuite testSuite = new TestSuite.Builder()
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
        .addEnumLevels("enum", TestCase.Category.class)
        .build();
    assertEquals(87, testSuite.size());
  }
}
