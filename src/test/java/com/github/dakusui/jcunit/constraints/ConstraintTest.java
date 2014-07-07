package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.constraints.constraintmanagers.ccs.CCSValueTupleSet;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ConstraintTest extends JCUnitBase {
  @Test
  public void simple() throws CUT, JCUnitException {
    Tuple tuple = new Tuple();
    tuple.put("hdd1", 0);
    tuple.put("hdd2", 1);

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(eq($("hdd1"), 0)).then(not(eq($("hdd2"), 0))).build();
    Assert.assertThat(2, CoreMatchers.is(rule.evaluate(tuple).size()));
    Assert.assertThat(0, CoreMatchers.is(rule.evaluate(tuple).get("hdd1")));
    Assert.assertThat(1, CoreMatchers.is(rule.evaluate(tuple).get("hdd2")));
  }

  @Test
  public void shortCut1() throws CUT, JCUnitException {
    Tuple tuple = new Tuple();
    tuple.put("notEvaluated1", 1);
    tuple.put("notEvaluated2", 1);
    tuple.put("notEvaluated3", 1);
    tuple.put("notEvaluated4", 1);
    tuple.put("notEvaluated5", 1);
    tuple.put("evaluated1", 1);
    tuple.put("evaluated2", 1);

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            or(
                and(
                    eq($("notEvaluated1"), 0), eq($("notEvaluated2"), 1)
                ),
                eq(("evaluated1"), 1)
            )
        ).then(
            not(
                eq($("evaluated2"), 1)
            )
        ).build();

    Tuple result = rule.evaluate(tuple);
    System.out.println(result);
  }

  @Test
  public void shortCut2() throws CUT, JCUnitException {
    Tuple tuple = new Tuple();
    tuple.put("notEvaluated1", 1);
    tuple.put("notEvaluated2", 1);
    tuple.put("notEvaluated3", 1);
    tuple.put("notEvaluated4", 1);
    tuple.put("notEvaluated5", 1);
    tuple.put("evaluated1", 1);
    tuple.put("evaluated2", 1);

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            or(
                and(
                    eq($("notEvaluated3"), 1), eq($("notEvaluated4"), 1),
                    eq($("notEvaluated5"), 0)
                ),
                and(
                    eq($("notEvaluated1"), 0), eq($("notEvaluated2"), 1)
                ),
                eq($("evaluated1"), 1),
                and(
                    eq($("notEvaluated1"), 1), eq($("notEvaluated2"), 1)
                )
            )
        ).then(
            not(
                eq($("evaluated2"), 0)
            )
        ).build();

    Tuple result = rule.evaluate(tuple);
    System.out.println(result);
  }

  @Test
  public void shortCut3() throws CUT, JCUnitException {
    Tuple tuple = new Tuple();
    tuple.put("notEvaluated1", 1);
    tuple.put("notEvaluated2", 1);
    tuple.put("notEvaluated3", 1);
    tuple.put("notEvaluated4", 1);
    tuple.put("notEvaluated5", 1);
    tuple.put("evaluated1", 1);
    tuple.put("evaluated2", 1);
    tuple.put("evaluated3", 1);
    tuple.put("evaluated4", 1);
    tuple.put("evaluated5", 1);

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            or(
                and(
                    eq($("notEvaluatedX"), 0), eq($("notEvaluated5"), 1)
                ),
                and(
                    eq($("notEvaluatedZ"), 1), eq($("notEvaluated2"), 1),
                    eq($("notEvaluated3"), 1)
                ),
                and(
                    eq($("notEvaluated1"), 0), eq($("notEvaluated5"), 1)
                ),
                eq($("evaluated1"), 1)
            )
        ).then(
            not(
                eq($("evaluated2"), 0)
            )
        ).build();

    Tuple result = rule.evaluate(tuple);
    System.out.println(result);
  }

  @Test
  public void constraintSet() throws Exception {
    Map<String, List<Object>> domains = composeDomains();
    CCSValueTupleSet valueTupleSet = new CCSValueTupleSet(domains);
    Tuple valueTuple = new Tuple();
    valueTupleSet.add(valueTuple);
  }

  private Map<String, List<Object>> composeDomains() {
    Map<String, List<Object>> ret = new HashMap<String, List<Object>>();
    ret.put("P1", Arrays.asList((Object) "P11", "P12"));
    ret.put("P2", Arrays.asList((Object) "P21", "P22"));
    ret.put("P3", Arrays.asList((Object) "P31", "P32", "P33"));
    return ret;
  }

}
