package com.github.dakusui.jcunit.tests.features.api;

import com.github.dakusui.jcunit.api.TestSuiteBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class TestSuiteBuilderTest {
  @Test
  public void test() {
    List<Tuple> testSuite = new TestSuiteBuilder()
        .addFactor("factor1", 1, 2, 3)
        .addConstraint(new Utils.Predicate<Tuple>() {
          @Override
          public boolean apply(Tuple in) {
            return !in.get("factor1").equals(1);
          }
        })
        .enableNegativeTests()
        .build();
    assertEquals(3, testSuite.size());
    assertEquals(2, testSuite.get(0).get("factor1"));
    assertEquals(3, testSuite.get(1).get("factor1"));
    assertEquals(1, testSuite.get(2).get("factor1"));
  }

  @Test
  public void test2() {
    List<Tuple> testSuite = new TestSuiteBuilder()
        .addFactor("factor1", 1, 2, 3)
        .addConstraint(new Utils.Predicate<Tuple>() {
          @Override
          public boolean apply(Tuple in) {
            return !in.get("factor1").equals(1);
          }
        })
        .disableNegativeTests()
        .build();
    assertEquals(2, testSuite.size());
    assertEquals(2, testSuite.get(0).get("factor1"));
    assertEquals(3, testSuite.get(1).get("factor1"));
  }

}
