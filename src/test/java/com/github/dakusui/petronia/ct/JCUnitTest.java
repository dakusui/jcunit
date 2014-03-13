package com.github.dakusui.petronia.ct;

import static org.junit.Assert.assertEquals;
import junit.framework.Assert;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.JUnitCore;
import org.junit.runner.Result;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.DefaultRuleSetBuilder;
import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.In.Domain;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.generators.BaseTestArrayGenerator;

public class JCUnitTest extends DefaultRuleSetBuilder {
  @RunWith(JCUnit.class)
  public abstract static class Base extends DefaultRuleSetBuilder {
    @In(
        domain = Domain.Method)
    public int in;

    public static int[] in() {
      return new int[] { 123 };
    }

    @Out
    public int out;

    @Test
    public void test() throws Exception {
      out = in + 1;
    }
  }

  public static class Error extends Base {
    @Override
    public void test() throws Exception {
      throw new RuntimeException();
    }
  }

  public static class Fail extends Base {
    @Override
    public void test() throws Exception {
      Assert.assertTrue(false);
    }
  }

  public static class MatchAndPass extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123),
                             is(get("out"), 124));
  }

  public static class CutOperatorMakesRuleSetIgnoreFollowingRules extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123))
                             .expect(is(get("out"), 124)).cut()
                             .incase(lt(get("in"), 999)) // Since 'in' is less
                                                         // than 999, this rule
                                                         // matches, too.
                             .expect(is(get("out"), 999)) // This must fail if
                                                          // this rule matches.
                         ;
  }

  public static class CutOperatorMakesRuleSetIgnoreFollowingAndOtherwiseRule
      extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123))
                             .expect(is(get("out"), 124)).cut()
                             .incase(lt(get("in"), 999)) // Since 'in' is less
                                                         // than 999, this rule
                                                         // matches, too.
                             .expect(is(get("out"), 999)) // This must fail if
                                                          // this rule matches.
                             .otherwise(false);
  }

  public static class MatchOneOfRulesAndPass_1 extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123))
                             .expect(is(get("out"), 124))
                             .incase(gt(get("in"), 999)) // since
                                                         // 'in'
                                                         // is
                                                         // less
                                                         // than
                                                         // 999,
                                                         // this
                                                         // DOESNT
                                                         // match
                             .expect(is(get("out"), 124));
  }

  public static class MatchOneOfRulesAndPass_2 extends Base {
    @Rule
    public RuleSet rules = ruleSet()
                             // The order of execution is different from the
                             // other's.
                             .incase(gt(get("in"), 999))
                             // since 'in' is less
                             // than 999, this
                             // DOESNT match
                             .expect(is(get("out"), 124))
                             .incase(is(get("in"), 123))
                             .expect(is(get("out"), 124));
  }

  public static class MatchNestedAndPass extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet().incase(is(get("in"), 123)).expect(
                                 is(get("out"), 124)));
  }

  public static class MatchNestedAndPassByOtherwise extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet().incase(is(get("in"), 124))
                                 .expect(is(get("out"), 999))
                                 .otherwise(is(get("out"), 124)));
  }

  public static class MatchNestedPassAndMakeSureOtherwiseRuleDoesntBreak extends
      Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet().incase(is(get("in"), 123))
                                 .expect(is(get("out"), 124))
                                 .otherwise(is(get("out"), 999)));
  }

  public static class MatchNoneOfNestedAndFail extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet()
                                 .incase(not(is(get("in"), 123)))
                                 // since
                                 // 'in'
                                 // is
                                 // 123,
                                 // this
                                 // doesn't
                                 // match.
                                 .expect(is(get("out"), 124))
                                 .incase(gt(get("in"), 999)) // since
                                                             // 'in'
                                                             // is
                                                             // 123,
                                                             // this
                                                             // does
                                                             // neither
                                                             // match.
                                 .expect(is(get("out"), 124)));
  }

  public static class MatchAllNestedAndPass extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet()
                                 .incase(is(get("in"), 123))
                                 .expect(is(get("out"), 124))
                                 .incase(lt(get("in"), 999))
                                 // since 'in' is
                                 // less than 999,
                                 // this also
                                 // matches.
                                 .expect(is(get("out"), 124))
                                 .otherwise(is(get("out"), 999)));
  }

  public static class MatchAllNestedAndMakeSureOtherwiseRuleDoesntBreak extends
      Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet()
                                 .incase(is(get("in"), 123))
                                 .expect(is(get("out"), 124))
                                 .incase(lt(get("in"), 999))
                                 // since 'in' is
                                 // less than 999,
                                 // this also
                                 // matches.
                                 .expect(is(get("out"), 124))
                                 .otherwise(is(get("out"), 999)));
  }

  public static class MatchOneOfNestedAndPass_1 extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet().incase(is(get("in"), 123))
                                 .expect(is(get("out"), 124))
                                 .incase(gt(get("in"), 999)) // since 'in' is
                                                             // less than 999,
                                                             // this DOESNT
                                                             // match
                                 .expect(is(get("out"), 124)));
  }

  public static class MatchOneOfNestedAndPass_2 extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123)).expect(
                             ruleSet()
                                 // The order of execution is different from the
                                 // other's.
                                 .incase(gt(get("in"), 999))
                                 // since 'in' is less
                                 // than 999, this
                                 // DOESNT match
                                 .expect(is(get("out"), 124))
                                 .incase(is(get("in"), 123))
                                 .expect(is(get("out"), 124)));
  }

  public static class MatchButFail extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123),
                             is(get("out"), 456));
  }

  public static class NoMatch extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 456),
                             is(get("out"), 124));

  }

  public static class OnePassOneFail_1 extends Base {
    @Rule
    public RuleSet rules = ruleSet().incase(is(get("in"), 123))
                             .expect(is(get("out"), 124))
                             .incase(lt(get("in"), 999)) // since
                                                         // 'in'
                                                         // is
                                                         // less
                                                         // than
                                                         // 999,
                                                         // this
                                                         // also
                                                         // matches
                             .expect(is(get("out"), 125));
  }

  public static class OnePassOneFail_2 extends Base {
    @Rule
    public RuleSet rules = ruleSet()
                             .incase(lt(get("in"), 999))
                             // since 'in' is
                             // less than
                             // 999, this
                             // also matches
                             .expect(is(get("out"), 125))
                             .incase(is(get("in"), 123))
                             .expect(is(get("out"), 124));
  }

  public static class PassByNestedOtherwise extends Base {
    @Rule
    public RuleSet rules = ruleSet()
                             .incase(is(get("in"), 999))
                             .expect(is(get("out"), 124))
                             .incase(gt(get("in"), 999))
                             // since 'in' is less than 999, this DOESNT match
                             .expect(is(get("out"), 124))
                             .otherwise(
                                 ruleSet().incase(is(get("in"), 123),
                                     is(get("out"), 124)));
  }

  public static class PassWithoutRules extends Base {
  }

  public static class BadGenerator_ConstructorWithParameters<T, U> extends
      BaseTestArrayGenerator<T, U> {
    public BadGenerator_ConstructorWithParameters(Object dummy1, Object dummy2) {
    }

    @Override
    public int getIndex(T key, long cur) {
      return -1;
    }
  }

  @RunWith(JCUnit.class)
  @Generator(BadGenerator_ConstructorWithParameters.class)
  public static class BadGenerator1 {
    @Test
    public void test() throws Exception {
    }
  }

  public static class BadGenerator_PrivateConstructor<T, U> extends
      BaseTestArrayGenerator<T, U> {
    private BadGenerator_PrivateConstructor() {
    }

    @Override
    public int getIndex(T key, long cur) {
      return -1;
    }
  }

  @RunWith(JCUnit.class)
  @Generator(BadGenerator_PrivateConstructor.class)
  public static class BadGenerator2 {
    @Test
    public void test() throws Exception {
    }
  }

  private void runTest(Class<?> testClass, int runCount, int failureCount,
      int ignoreCount) {
    Result result = JUnitCore.runClasses(testClass);
    assertEquals(runCount, result.getRunCount());
    assertEquals(failureCount, result.getFailureCount());
    assertEquals(ignoreCount, result.getIgnoreCount());
  }

  @Test
  public void error() throws Exception {
    runTest(Error.class, 1, 1, 0);
  }

  @Test
  public void fail() throws Exception {
    runTest(Fail.class, 1, 1, 0);
  }

  @Test
  public void matchAllNestedAndMakeSureOtherwiseRuleDoesntBreak()
      throws Exception {
    runTest(MatchAllNestedAndMakeSureOtherwiseRuleDoesntBreak.class, 1, 0, 0);
  }

  @Test
  public void matchAllNestedAndPass() throws Exception {
    runTest(MatchAllNestedAndPass.class, 1, 0, 0);
  }

  @Test
  public void matchAndPass() throws Exception {
    runTest(MatchAndPass.class, 1, 0, 0);
  }

  @Test
  public void matchNestedAndPass() throws Exception {
    runTest(MatchNestedAndPass.class, 1, 0, 0);
  }

  @Test
  public void matchNestedPassAndMakeSureOtherwiseRuleDoesntBreak()
      throws Exception {
    runTest(MatchNestedPassAndMakeSureOtherwiseRuleDoesntBreak.class, 1, 0, 0);
  }

  @Test
  public void matchNoneOfNestedAndFail() throws Exception {
    runTest(MatchNoneOfNestedAndFail.class, 1, 1, 0);
  }

  @Test
  public void matchOneOfNestedAndPass_1() throws Exception {
    runTest(MatchOneOfNestedAndPass_1.class, 1, 0, 0);
  }

  @Test
  public void matchOneOfNestedAndPass_2() throws Exception {
    runTest(MatchOneOfNestedAndPass_2.class, 1, 0, 0);
  }

  @Test
  public void matchOneOfRulesAndPass_1() throws Exception {
    runTest(MatchOneOfRulesAndPass_1.class, 1, 0, 0);
  }

  @Test
  public void matchOneOfRulesAndPass_2() throws Exception {
    runTest(MatchOneOfRulesAndPass_2.class, 1, 0, 0);
  }

  @Test
  public void matchButFail() throws Exception {
    runTest(MatchButFail.class, 1, 1, 0);
  }

  @Test
  public void noMatch() throws Exception {
    runTest(NoMatch.class, 1, 1, 0);
  }

  @Test
  public void onePassOneFail_1() throws Exception {
    runTest(OnePassOneFail_1.class, 1, 1, 0);
  }

  @Test
  public void onePassOneFail_2() throws Exception {
    runTest(OnePassOneFail_2.class, 1, 1, 0);
  }

  @Test
  public void passByNestedOtherwise() throws Exception {
    runTest(PassByNestedOtherwise.class, 1, 0, 0);
  }

  @Test
  public void passWithoutRules() throws Exception {
    runTest(PassWithoutRules.class, 1, 0, 0);
  }

  @Test
  public void badGenerator1() throws Exception {
    runTest(BadGenerator1.class, 1, 1, 0);
  }

  @Test
  public void badGenerator2() throws Exception {
    runTest(BadGenerator2.class, 1, 1, 0);
  }

  @Test
  public void cutOperatorMakesRuleSetIgnoreFollowingRules() throws Exception {
    runTest(CutOperatorMakesRuleSetIgnoreFollowingRules.class, 1, 0, 0);
  }

  @Test
  public void cutOperatorMakesRuleSetIgnoreFollowingAndOtherwiseRule()
      throws Exception {
    runTest(CutOperatorMakesRuleSetIgnoreFollowingAndOtherwiseRule.class, 1, 0,
        0);
  }
}
