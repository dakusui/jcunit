package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.compat.core.JCUnitBase;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.lisj.CUT;
import com.github.dakusui.lisj.exceptions.LisjCheckedException;
import com.github.dakusui.lisj.exceptions.SymbolNotFoundException;
import org.hamcrest.CoreMatchers;
import org.junit.Assert;
import org.junit.Test;

public class ConstraintTest extends JCUnitBase {
  @Test(expected = SymbolNotFoundException.class)
  public void notAbleToEvaluateForInsufficientAttributes()
      throws CUT, LisjCheckedException {
    Tuple.Builder builder = new Tuple.Builder();
    builder.put("evaluated1", 1);
    builder.put("evaluated2", 1);
    Tuple t = builder.build();

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            and(
                eq($("evaluated1"), 1), eq($("missing"), 1)
            )
        ).then(
            not(
                eq($("evaluated2"), 1)
            )
        ).build();

    Assert.fail(String
        .format("This path shouldn't be executed. '%s'", rule.evaluate(t)));
  }

  @Test
  public void tupleSatisfiesSimpleConstraint()
      throws CUT, LisjCheckedException {
    Tuple.Builder builder = new Tuple.Builder();
    builder.put("evaluated1", 1);
    builder.put("evaluated11", 11);
    builder.put("evaluated2", 2);
    Tuple t = builder.build();

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            and(
                eq($("evaluated1"), 1), eq($("evaluated11"), 11)
            )
        ).then(
            not(
                eq($("evaluated2"), 0)
            )
        ).build();

    Tuple result = rule.evaluate(t);
    Assert.assertThat(result, CoreMatchers.nullValue());
  }

  @Test
  public void tupleSatisfiesConstraintWithShortCut()
      throws CUT, LisjCheckedException {
    Tuple.Builder builder = new Tuple.Builder();
    builder.put("attr1", 1);
    builder.put("attr1sc", 99);
    builder.put("attr2", 2);
    builder.put("attr3", 3);
    Tuple t = builder.build();

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            or(
                and(
                    eq($("attr1"), 0), eq($("attr1sc"), 99)
                ),
                eq($("attr2"), 2)
            )
        ).then(
            eq($("attr3"), 3)
        ).build();

    Tuple result = rule.evaluate(t);
    System.out.println(result);
    Assert.assertThat(result, CoreMatchers.nullValue());
  }

  @Test
  public void tupleDoesntSatisfySimpleConstraintInWhenClause()
      throws CUT, LisjCheckedException {
    Tuple.Builder builder = new Tuple.Builder();
    builder.put("evaluated1", 1);
    builder.put("evaluated2", 2);
    Tuple t = builder.build();

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            eq($("evaluated1"), 0)
        ).then(
            eq($("evaluated2"), 1)
        ).build();

    Tuple result = rule.evaluate(t);
    Assert.assertThat(1, CoreMatchers.is(result.size()));
    Assert.assertThat(1, CoreMatchers.is(result.get("evaluated1")));
  }

  @Test
  public void tupleDoesntSatisfySimpleConstraintInThenClause()
      throws CUT, LisjCheckedException {
    Tuple.Builder builder = new Tuple.Builder();
    builder.put("evaluated1", 1);
    builder.put("evaluated2", 2);
    Tuple t = builder.build();

    ConstraintRule rule = new ConstraintRule.Builder().setContext(this)
        .when(
            eq($("evaluated1"), 1)
        ).then(
            eq($("evaluated2"), 0)
        ).build();

    Tuple result = rule.evaluate(t);
    Assert.assertThat(2, CoreMatchers.is(result.size()));
    Assert.assertThat((Integer) result.get("evaluated1"), CoreMatchers.is(1));
    Assert.assertThat((Integer) result.get("evaluated2"), CoreMatchers.is(2));
  }
}
