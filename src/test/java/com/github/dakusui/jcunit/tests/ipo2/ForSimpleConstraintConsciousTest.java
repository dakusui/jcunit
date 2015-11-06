package com.github.dakusui.jcunit.tests.ipo2;

import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.IPO2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.IPO2Optimizer;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Test;

import java.util.*;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class ForSimpleConstraintConsciousTest extends IPO2Test {
  private List<Tuple> prohibitedTuples = null;

  public List<Tuple> getProhibitedTuples() {
    return this.prohibitedTuples;
  }

  protected void setProhibitedTuples(Tuple... tuples) {
    this.prohibitedTuples = new LinkedList<Tuple>();
    Collections.addAll(prohibitedTuples, tuples);
  }

  @Override
  public Constraint createConstraintManager() {
    return new TestConstraint(getProhibitedTuples());
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
    Constraint constraint = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraint, optimizer);
    verify(strength, factors, constraint, ipo.getResult(),
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
    Constraint constraint = createConstraintManager();
    IPO2Optimizer optimizer = createOptimizer();

    IPO2 ipo = createIPO2(factors,
        strength, constraint, optimizer);
    verify(strength, factors, constraint, ipo.getResult(),
        ipo.getRemainders());
  }

  @Override
  protected void verifyRemaindersViolateConstraints(List<Tuple> remainders,
      List<Tuple> result, Constraint constraint) {
    // Since in this test class there is no implicit constraint, we
    // can simply verify them.
    UTUtils.stdout().println(result);
    for (Tuple tuple : remainders) {
      assertThat(String.format("'%s' is contained in result set.", tuple),
          find(tuple, result), is(false));
      assertThat(String.format("'%s' doesn't violate any constraints.", tuple),
          checkConstraints(
              constraint,
              tuple), is(false));
    }
  }

  public static class TestConstraint implements Constraint {
    private final Set<Tuple> constraints;

    TestConstraint(List<Tuple> constraints) {
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
    public Factors getFactors() {
      return null;
    }

    @Override
    public void setFactors(Factors factors) {
    }

    @Override
    public void addObserver(Observer observer) {
    }

    @Override
    public Set<Observer> observers() {
      return null;
    }

    @Override
    public void removeObservers(Observer observer) {

    }

    @Override
    public List<Tuple> getViolations() {
      return Collections.emptyList();
    }
  }
}
