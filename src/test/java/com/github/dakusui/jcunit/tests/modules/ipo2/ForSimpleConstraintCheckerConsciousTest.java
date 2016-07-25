package com.github.dakusui.jcunit.tests.modules.ipo2;

import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.Ipo2Optimizer;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ForSimpleConstraintCheckerConsciousTest extends Ipo2Test {
  private List<Tuple> prohibitedTuples = null;

  public List<Tuple> getProhibitedTuples() {
    return this.prohibitedTuples;
  }

  protected void setProhibitedTuples(Tuple... tuples) {
    this.prohibitedTuples = new LinkedList<Tuple>();
    Collections.addAll(prohibitedTuples, tuples);
  }

  @Override
  public ConstraintChecker createConstraintManager() {
    return new TestConstraintChecker(getProhibitedTuples());
  }

  @Test
  public void test_001a() {
    int strength = 2;
    this.setProhibitedTuples(
        new Tuple.Builder().put("F1", "L1x").put("F2", "L2x").build());

    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L1x"))
        .add(factor("F2", "L21", "L2x"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    Ipo2Optimizer optimizer = createOptimizer();

    Ipo2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(),
        ipo.getRemainders());
  }

  @Test
  public void test_001b() {
    int strength = 3;
    this.setProhibitedTuples(
        new Tuple.Builder().put("F1", "L1x").put("F2", "L2x").build());

    Factors factors = new Factors.Builder()
        .add(factor("F1", "L11", "L1x"))
        .add(factor("F2", "L21", "L2x"))
        .add(factor("F3", "L31"))
        .add(factor("F4", "L41", "L42")).build();
    ConstraintChecker constraintChecker = createConstraintManager();
    Ipo2Optimizer optimizer = createOptimizer();

    Ipo2 ipo = createIPO2(factors,
        strength, constraintChecker, optimizer);
    verify(strength, factors, constraintChecker, ipo.getResult(),
        ipo.getRemainders());
  }

  @Override
  protected void verifyRemaindersViolateConstraints(List<Tuple> remainders,
      List<Tuple> result, ConstraintChecker constraintChecker) {
    // Since in this test class there is no implicit constraint, we
    // can simply verify them.
    UTUtils.stdout().println(result);
    for (Tuple tuple : remainders) {
      assertThat(String.format("'%s' is contained in result set.", tuple),
          find(tuple, result), is(false));
      assertThat(String.format("'%s' doesn't violate any constraints.", tuple),
          checkConstraints(
              constraintChecker,
              tuple), is(false));
    }
  }

  public static class TestConstraintChecker extends Plugin.Base implements ConstraintChecker {
    private final Set<Tuple> constraints;

    TestConstraintChecker(List<Tuple> constraints) {
      this.constraints = new HashSet<Tuple>();
      this.constraints.addAll(constraints);
    }

    private static boolean matches(Tuple constraint, Tuple t) {
      for (String fName : constraint.keySet()) {
        if (!Utils.eq(constraint.get(fName), t.get(fName))) {
          return false;
        }
      }
      return true;
    }

    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      List<String> missings = new LinkedList<String>();
      for (Tuple c : constraints) {
        for (String each : c.keySet()) {
          if (!tuple.keySet().contains(each)) {
            missings.add(each);
          }
        }
        if (matches(c, tuple)) {
          return false;
        }
      }
      if (!missings.isEmpty()) {
        throw new UndefinedSymbol(missings.toArray(new String[missings.size()]));
      }
      return true;
    }

    @Override
    public List<Tuple> getViolations() {
      return Collections.emptyList();
    }

    @Override
    public List<String> getTags() {
      return Collections.emptyList();
    }

    @Override
    public boolean violates(Tuple tuple, String constraintTag) {
      return false;
    }
  }
}
