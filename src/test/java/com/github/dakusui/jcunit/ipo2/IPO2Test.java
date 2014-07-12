package com.github.dakusui.jcunit.ipo2;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.constraints.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.generators.ipo2.IPO2;
import com.github.dakusui.jcunit.generators.ipo2.IPO2Utils;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.GreedyIPO2Optimizer;
import com.github.dakusui.jcunit.generators.ipo2.optimizers.IPO2Optimizer;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public abstract class IPO2Test {
  protected static Factor factor(String name, Object... factors) {
    return new Factor(name, Arrays.asList(factors));
  }

  protected IPO2 generate(
      Factors factors, int strength,
      ConstraintManager constraintManager,
      IPO2Optimizer optimizer) {
    IPO2 ipo = new IPO2(factors, strength, constraintManager,
        optimizer);
    ipo.ipo();
    return ipo;
  }

  protected void verify(List<Tuple> testcases,
      List<Tuple> remainders, int strength, Factors factors,
      ConstraintManager constraintManager) {
    System.out.println(String.format("%3d:%s", testcases.size(), testcases));
    verifyAllValidTuplesAreGenerated(testcases, strength, factors,
        constraintManager);
    verifyNoConstraintViolationOccursInResult(testcases, constraintManager);
    verifyNoDuplicationOccursInResult(testcases);
    verifyAllTestCasesHaveCorrectNumberOfAttributes(testcases, factors);
    verifyRemaindersViolateConstraints(remainders, testcases, constraintManager);
  }

  protected void verifyRemaindersViolateConstraints(List<Tuple> remainders,
      List<Tuple> result, ConstraintManager constraintManager) {
    ////
    // No entry in remainder is expected in default implementation.
    assertThat(String.format("Some remainder(s) are found.: %s", remainders), remainders.size(), is(0));
  }

  protected void verifyAllTestCasesHaveCorrectNumberOfAttributes(
      List<Tuple> testcases, Factors factors) {
    ////
    // All the test cases have correct number of attributes.
    List<Tuple> wrongTestCasesInvalidNumAttributes = new LinkedList<Tuple>();
    for (Tuple t : testcases) {
      if (t.size() != factors.size()) {
        wrongTestCasesInvalidNumAttributes.add(t);
      }
    }
    assertThat(String
            .format("%d test cases have wrong number of attributes. %s",
                wrongTestCasesInvalidNumAttributes.size(),
                wrongTestCasesInvalidNumAttributes),
        wrongTestCasesInvalidNumAttributes.size(), is(0)
    );
  }

  protected void verifyNoDuplicationOccursInResult(List<Tuple> testcases) {
    ////
    // No duplication.
    Set<Tuple> duplicated = new HashSet<Tuple>();
    Set<Tuple> checked = new HashSet<Tuple>();
    for (Tuple t : testcases) {
      if (checked.contains(t)) {
        duplicated.add(t);
      } else {
        checked.add(t);
      }
    }
    assertThat(String
        .format("%d test cases are duplicated. %s", duplicated.size(),
            duplicated), duplicated.size(), is(0));
  }

  protected void verifyNoConstraintViolationOccursInResult(List<Tuple> testcases,
      ConstraintManager constraintManager) {
    ///
    // No violation.
    List<Tuple> violations = new LinkedList<Tuple>();
    for (Tuple t : testcases) {
      if (!constraintManager.check(t)) {
        violations.add(t);
      }
    }
    assertThat(String
        .format("%d tuples are violating constraints. %s", violations.size(),
            violations
        ), violations.size(), is(0));
  }

  protected void verifyAllValidTuplesAreGenerated(List<Tuple> testcases,
      int strength, Factors factors, ConstraintManager constraintManager) {
    ////
    // All tuples (excepting prohibited ones) are covered.
    List<Tuple> tuplesToBeGenerated = factors
        .generateAllPossibleTuples(strength);
    List<Tuple> notFound = new LinkedList<Tuple>();
    for (Tuple t : tuplesToBeGenerated) {
      if (!constraintManager.check(t)) {
        continue;
      }
      if (!find(t, testcases)) {
        notFound.add(t);
      }
    }
    assertThat(String
            .format("%d tuples were not found. %s", notFound.size(), notFound),
        notFound.size(), is(0)
    );
  }

  protected boolean find(Tuple t,
      List<Tuple> tuples) {
    for (Tuple cur : tuples) {
      if (matches(t, cur)) {
        return true;
      }
    }
    return false;
  }

  private boolean matches(Tuple q,
      Tuple tuple) {
    if (!tuple.keySet().containsAll(q.keySet())) return false;
    for (String k : q.keySet()) {
      if (!IPO2Utils.eq(q.get(k), tuple.get(k))) {
        return false;
      }
    }
    return true;
  }

  protected ConstraintManager createConstraintManager() {
    return new NullConstraintManager();
  }

  protected GreedyIPO2Optimizer createOptimizer() {
    return new GreedyIPO2Optimizer();
  }
}
