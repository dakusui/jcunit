package com.github.dakusui.jcunit8.tests.features.seed;

import com.github.dakusui.crest.core.Printable;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.notification.Failure;

import java.util.function.Function;

import static com.github.dakusui.crest.matcherbuilders.Crest.*;

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
 *        3. No test case that violates Cb is generated except for Sa.
 *
 * Check points
 * - tuple covered by seeds shouldn't be placed in generated regular test cases
 * - all the possible tuples should be covered by regular test cases
 * - constraint violated by a seed without violating any others shouldn't be covered
 *   by generated negative test cases if negative test generation is enabled.
 * - all the constraints should be violated by negative test cases.
 */
public class SeedTest {
  @Test
  public void simpleSeeds() {
    assertThat(
        JUnitCore.runClasses(SeedFeatureTestBase.SeedsForSimpleParameters.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").eq(5).$(),
            asInteger("getFailureCount").eq(5).$(),
            asString(messageOfFailureAt(0)).containsString("a=0,b=0,c=0").$(),
            asString(messageOfFailureAt(1)).containsString("a=1,b=1,c=1").$()
        )
    );
  }

  @Test
  public void simpleSeedsWithConstraint() {
    assertThat(
        JUnitCore.runClasses(SeedFeatureTestBase.SeedsForSimpleParametersWithConstraint.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").eq(6).$(),
            asInteger("getFailureCount").eq(6).$(),
            asString(messageOfFailureAt(0)).containsString("a=0,b=0,c=0").$(),
            asListOf(
                Failure.class,
                Printable.function(
                    "getFailures", Result::getFailures
                ).andThen(Printable.function(
                    "subList", list -> list.subList(1, list.size())
                ))).noneMatch(
                Printable.predicate(
                    "b=0,c=0", (Failure f) -> f.getMessage().contains("b=0,c=0")
                )
            ).$()
        )
    );
  }

  @Test
  public void simpleSeedWithConstraint() {
    ////
    // This test makes sure once constraint violation b != c is violated by a seed,
    // JCUnit doesn't try to cover it again even if negative test generation is
    // enabled.
    assertThat(
        JUnitCore.runClasses(SeedFeatureTestBase.SeedForSimpleParametersWithConstraint.class),
        allOf(
            asBoolean("wasSuccessful").isFalse().$(),
            asInteger("getRunCount").eq(6).$(),
            asInteger("getFailureCount").eq(6).$(),
            asString(messageOfFailureAt(0)).containsString("a=0,b=0,c=0").$(),
            asListOf(
                Failure.class,
                Printable.function(
                    "getFailures", Result::getFailures
                ).andThen(Printable.function(
                    "subList", list -> list.subList(1, list.size())
                ))).noneMatch(
                Printable.predicate(
                    "b=0,c=0", (Failure f) -> f.getMessage().contains("b=0,c=0")
                )
            ).$()
        )
    );
  }

  private Function<Result, String> messageOfFailureAt(int i) {
    return Printable.function("messageOfFailureAt[" + i + "]", (Result r) -> r.getFailures().get(i).getMessage());
  }
}
