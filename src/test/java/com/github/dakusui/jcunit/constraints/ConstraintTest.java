package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.constraints.ccs.CCSValueTuple;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.core.ValueTuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.lisj.CUT;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

/**
 * Created by hiroshi on 5/23/14.
 */
public class ConstraintTest extends JCUnitBase {
  @Test
  public void simple() throws CUT, JCUnitException {
    ValueTuple<String, Object> tuple = new ValueTuple<String, Object>();
    tuple.put("hdd1", 0);
    tuple.put("hdd2", 1);

    ConstraintRule rule = new ConstraintRule(this);
    rule.when(eq($("hdd1"), 0)).then(not(eq($("hdd2"), 0)));
    Assert.assertThat(2, CoreMatchers.is(rule.evaluate(tuple).size()));
    Assert.assertThat(0, CoreMatchers.is(rule.evaluate(tuple).get("hdd1")));
    Assert.assertThat(1, CoreMatchers.is(rule.evaluate(tuple).get("hdd2")));
  }

  @Test
  public void shortCut1() throws CUT, JCUnitException {
    ValueTuple<String, Object> tuple = new ValueTuple<String, Object>();
    tuple.put("notEvaluated1", 1);
    tuple.put("notEvaluated2", 1);
    tuple.put("notEvaluated3", 1);
    tuple.put("notEvaluated4", 1);
    tuple.put("notEvaluated5", 1);
    tuple.put("evaluated1", 1);
    tuple.put("evaluated2", 1);

    ConstraintRule rule = new ConstraintRule(this);
    rule.when(
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
    );


    CCSValueTuple<String, Object> result = rule.evaluate(tuple);
    System.out.println(result);
  }

  @Test
  public void shortCut2() throws CUT, JCUnitException {
    ValueTuple<String, Object> tuple = new ValueTuple<String, Object>();
    tuple.put("notEvaluated1", 1);
    tuple.put("notEvaluated2", 1);
    tuple.put("notEvaluated3", 1);
    tuple.put("notEvaluated4", 1);
    tuple.put("notEvaluated5", 1);
    tuple.put("evaluated1", 1);
    tuple.put("evaluated2", 1);

    ConstraintRule rule = new ConstraintRule(this);
    rule.when(
        or(
            and(
                eq($("notEvaluated3"), 1), eq($("notEvaluated4"), 1), eq($("notEvaluated5"), 0)
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
    );


    CCSValueTuple<String, Object> result = rule.evaluate(tuple);
    System.out.println(result);
  }

  @Test
  public void shortCut3() throws CUT, JCUnitException {
    ValueTuple<String, Object> tuple = new ValueTuple<String, Object>();
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

    ConstraintRule rule = new ConstraintRule(this);
    rule.when(
        or(
            and(
                eq($("notEvaluated1"), 1), eq($("notEvaluated2"), 1), eq($("notEvaluated3"), 0)
            ),
            and(
                eq($("notEvaluated4"), 0), eq($("notEvaluated5"), 1)
            ),
            eq($("evaluated1"), 1)
        )
    ).then(
        not(
            eq($("evaluated2"), 0)
        )
    );


    CCSValueTuple<String, Object> result = rule.evaluate(tuple);
    System.out.println(result);
  }

}
