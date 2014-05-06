package com.github.dakusui.petronia.examples;

import org.junit.ClassRule;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;
import org.junit.runner.RunWith;

import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.Out;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.In.Domain;
import com.github.dakusui.jcunit.core.BasicSummarizer;
import com.github.dakusui.jcunit.core.Summarizer;
import com.github.dakusui.jcunit.generators.CartesianTestArrayGenerator;
import com.github.dakusui.petronia.examples.Calc.Op;

/**
 * Since <code>Calc</code> has some intentional bugs, this test class will
 * produce the results below.
 * 
 * <pre>
 * - Failures  :  43 (Overflow isn't reported/rounding errors)
 * - Errors    :   7 (Division by zero)  
 * - Total runs: 245
 * </pre>
 * 
 * @author hiroshi
 */
@RunWith(JCUnit.class)
@Generator(CartesianTestArrayGenerator.class)
public class CalcTest2 extends JCUnitBase {
  @Rule
  @In(
      domain = Domain.None)
  public TestName          name       = new TestName();
  @In
  public int               a;
  @In
  public int               b;
  @In
  public Op                op;
  @Out
  public int               r;
  @Out
  public Throwable         t;
  @Rule
  public RuleSet           rules      = ruleSet()
                                          .incase(
                                              is(get("op"), null),
                                              isinstanceof(get("t"),
                                                  NullPointerException.class))
                                          .incase(
                                              is(get("op"), Op.plus),
                                              ruleSet()
                                                  .incase(
                                                      not(or(
                                                          and(isoneof(
                                                              Integer.MIN_VALUE,
                                                              get("a"),
                                                              get("b")),
                                                              lt(max(get("a"),
                                                                  get("b")), 0)),
                                                          and(isoneof(
                                                              Integer.MAX_VALUE,
                                                              get("a"),
                                                              get("b")),
                                                              gt(min(get("a"),
                                                                  get("b")), 0)))))
                                                  .expect(
                                                      is(get("r"),
                                                          add(get("a"),
                                                              get("b"))))
                                                  .cut()
                                                  .otherwise(
                                                      isinstanceof(
                                                          get("t"),
                                                          RuntimeException.class)))
                                          .incase(
                                              is(get("op"), Op.minus),
                                              is(get("r"),
                                                  sub(get("a"), get("b"))))
                                          .incase(
                                              is(get("op"), Op.multiply),
                                              is(get("r"),
                                                  mul(get("a"), get("b"))))
                                          .incase(
                                              is(get("op"), Op.divide),
                                              is(get("r"),
                                                  div(get("a"), get("b"))))
                                          .summarizer(summarizer);

  @ClassRule
  public static Summarizer summarizer = new BasicSummarizer();

  @Test
  public void test() {
    try {
      Calc calc = new Calc();
      r = calc.calc(op, a, b);
    } catch (RuntimeException e) {
      t = e;
    }
  }
}
