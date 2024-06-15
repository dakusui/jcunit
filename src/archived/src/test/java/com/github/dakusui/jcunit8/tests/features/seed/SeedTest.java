package com.github.dakusui.jcunit8.tests.features.seed;

import com.github.dakusui.crest.utils.printable.Functions;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.JUnitCore;

import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.crest.Crest.*;

/*
 * Seed feature test.
 *
 * Seed(s)
 * - Sa : {"a":0, "b":0, "c":0 } conforms Ca, violates Cb
 * - Sb : {"a":0, "b":1, "c":1 } violates Ca, violates Cb
 *
 * Constraint(s)
 * - Ca : a == b
 * - Cb : b != c
 *
 * Negative test generation
 * - enabled
 * - disabled
 *
 * Test cases
 * - T01: Seeds:[Sa], Constraints:[Ca], Negative:disabled;
 *        1. Sa is at 0.
 *        2. {"a":0,"b":0},{"b":0,"c":0},{"c":0,"c":0} are not covered too much.
 *        3. Check neither Ca nor Cb are violated
 * - T02: Seeds:[Sa], Constraints:[Ca], Negative:enabled;
 *        1. Check T01 is generated.
 *        2. {"a":0,"b":0},{"b":0,"c":0},{"c":0,"c":0} are not covered too much.
 *        3. Check neither Ca nor Cb are violated by test cases exception for the last two.
 *        4. 2 test cases that violate Ca and Cb are generated and placed at the end
 *           of the suite.
 * - T03: Seeds:[Sa], Constraints:[Cb], Negative:enabled;
 *        1. Sa is at 0.
 *        2. No test case that violates Cb is generated except for Sa.
 *        3. A test case that violates Ca is at the end of the suite.
 * - T04: Seeds:[Sa,Sb], Constraints:[Cb], Negative:enabled;
 *        1. Sa is at 0.
 *        2. Sb is at 1.
 *        3. No test case that violates Cb is generated except for Sa and Sb.
 * - T05: Seeds:[Sb], Constraints:[Cb], Negative:enabled;

 * Check points
 * - tuple covered by seeds shouldn't be placed in generated regular test cases
 * - all the possible tuples should be covered by regular test cases
 * - constraint violated by a seed without violating any others shouldn't be covered
 *   by generated negative test cases if negative test generation is enabled.
 * - all the constraints should be violated by negative test cases.
 * - Test suite
 *   t-way coverage
 *   Constraint coverage (when negative test generation enabled)
 *   no redundant test cases
 */
public class SeedTest {
  @Test
  public void runT00() {
    assertThat(
        generateTestCasesByRunningTestClass(SeedFeatureTestBase.T00.class),
        allOf(
            asInteger(Functions.size()).equalTo(4).$()
        )
    );
  }

  @Test
  public void runT01() {
    assertThat(
        generateTestCasesByRunningTestClass(SeedFeatureTestBase.T01.class),
        allOf(
            asInteger(Functions.size()).equalTo(4).$()
        )
    );
  }

  @Test
  public void runT02() {
    assertThat(
        generateTestCasesByRunningTestClass(SeedFeatureTestBase.T02.class),
        allOf(
            asInteger(Functions.size()).equalTo(5).$()
        )
    );
  }

  @Test
  public void runT03() {
    assertThat(
        generateTestCasesByRunningTestClass(SeedFeatureTestBase.T03.class),
        allOf(
            asInteger(Functions.size()).equalTo(5).$()
        )
    );
  }

  @Test
  public void runT04() {
    assertThat(
        generateTestCasesByRunningTestClass(SeedFeatureTestBase.T04.class),
        allOf(
            asInteger(Functions.size()).equalTo(5).$()
        )
    );
  }

  @Test
  public void runT05() {
    assertThat(
        generateTestCasesByRunningTestClass(SeedFeatureTestBase.T05.class),
        allOf(
            asInteger(Functions.size()).equalTo(5).$()
        )
    );
  }

  private List<Tuple> generateTestCasesByRunningTestClass(Class klass) {
    synchronized (SeedFeatureTestBase.testCases) {
      SeedFeatureTestBase.testCases.clear();
      Assert.assertTrue(JUnitCore.runClasses(klass).wasSuccessful());
      return new LinkedList<>(SeedFeatureTestBase.testCases);
    }
  }
}
