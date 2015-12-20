package com.github.dakusui.jcunit.tests.modules.ipo2;

import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.constraints.NullConstraintChecker;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.IPO2;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.GreedyIPO2Optimizer;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.optimizers.IPO2Optimizer;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;

import java.util.*;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;

public abstract class IPO2Test {
  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

  protected static Factor factor(String name, Object... factors) {
    return new Factor(name, Arrays.asList(factors));
  }

  protected static FactorsDef factorsDef(int l, int f) {
    return new FactorsDef(l, f);
  }

  protected static Factors buildFactors(FactorsDef... factorsDefs) {
    Factors.Builder fb = new Factors.Builder();
    char ch = 'A';
    for (FactorsDef fd : factorsDefs) {
      for (int i = 0; i < fd.numFactors; i++) {
        Factor.Builder b = new Factor.Builder(Character.toString(ch));
        for (int j = 0; j < fd.numLevels; j++) {
          b.addLevel(Character.toString(ch) + j);
        }
        ch++;
        fb.add(b.build());
      }
    }
    return fb.build();
  }

  protected IPO2 createIPO2(
      Factors factors, int strength,
      ConstraintChecker constraintChecker,
      IPO2Optimizer optimizer) {
    IPO2 ipo = new IPO2(factors, strength, constraintChecker,
        optimizer);
    ipo.ipo();
    return ipo;
  }

  protected void verify(int givenStrength, Factors givenFactors, ConstraintChecker givenConstraintChecker, List<Tuple> actualTestCases,
      List<Tuple> actualRemainders) {
    UTUtils.stdout().println(String.format("%3d:%s", actualTestCases.size(), actualTestCases));
    verifyAllValidTuplesAreGenerated(actualTestCases, givenStrength, givenFactors,
        givenConstraintChecker);
    verifyNoConstraintViolationOccursInResult(actualTestCases, givenConstraintChecker);
    verifyNoDuplicationOccursInResult(actualTestCases);
    verifyAllTestCasesHaveCorrectNumberOfAttributes(actualTestCases, givenFactors);
    verifyRemaindersViolateConstraints(actualRemainders, actualTestCases, givenConstraintChecker);
  }

  protected void verifyRemaindersViolateConstraints(List<Tuple> remainders,
      List<Tuple> result, ConstraintChecker constraintChecker) {
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
      ConstraintChecker constraintChecker) {
    ///
    // No violation.
    List<Tuple> violations = new LinkedList<Tuple>();
    for (Tuple t : testcases) {
      if (!checkConstraints(constraintChecker, t)) {
        violations.add(t);
      }
    }
    assertThat(String
        .format("%d tuples are violating constraints. %s", violations.size(),
            violations
        ), violations.size(), is(0));
  }

  public boolean checkConstraints(ConstraintChecker constraintChecker, Tuple t) {
    try {
      return constraintChecker.check(t);
    } catch (UndefinedSymbol e) {
      ////
      // Consider failure from insufficient attributes is kind of success.
      return true;
    }
  }

  protected void verifyAllValidTuplesAreGenerated(List<Tuple> testcases,
      int strength, Factors factors, ConstraintChecker constraintChecker) {
    ////
    // All tuples (excepting prohibited ones) are covered.
    List<Tuple> tuplesToBeGenerated = factors
        .generateAllPossibleTuples(strength);
    List<Tuple> notFound = new LinkedList<Tuple>();
    for (Tuple t : tuplesToBeGenerated) {
      if (!checkConstraints(constraintChecker, t)) {
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
    if (!tuple.keySet().containsAll(q.keySet())) {
      return false;
    }
    for (String k : q.keySet()) {
      if (!Utils.eq(q.get(k), tuple.get(k))) {
        return false;
      }
    }
    return true;
  }

  protected ConstraintChecker createConstraintManager() {
    return new NullConstraintChecker();
  }

  protected GreedyIPO2Optimizer createOptimizer() {
    return new GreedyIPO2Optimizer();
  }

  public static class FactorsDef {
    int numLevels;
    int numFactors;

    public FactorsDef(int numLevels, int numFactors) {
      this.numLevels = numLevels;
      this.numFactors = numFactors;
    }
  }
}
