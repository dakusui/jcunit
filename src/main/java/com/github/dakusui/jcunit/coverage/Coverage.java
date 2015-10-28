package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A class to measure t-way coverage.
 */
public class Coverage {
  public final   int               degree;
  public final   int               initialSize;
  public final   Factors           factorSpace;
  private final   Set<Tuple>        yetToBeCovered;
  private final   Set<Tuple>        uncoveredInWeakerDegree;
  private final   Set<Tuple>        violations;
  public final   ConstraintManager cm;
  public final   Report            report;
  private int               covered;
  protected State state = State.NOT_PROCESSED;

  /**
   * Creates an object of this class.
   *
   * @param uncoveredInWeakerDegree Tuples whose degree are less than {@code degree} and known impossible.
   * @param factors                 A factor space where test suite's coverage is examined.
   * @param degree                  Strength (number of attributes involved).
   */
  public Coverage(
      Factors factors,
      final int degree,
      ConstraintManager cm,
      final Set<Tuple> uncoveredInWeakerDegree,
      Report report) {
    Checks.checkcond(degree > 0);
    Checks.checkcond(degree <= factors.size(), "degree=%s, factors.size=%s", degree, factors.size());
    this.factorSpace = Checks.checknotnull(factors);
    this.degree = degree;
    this.report = Checks.checknotnull(report);
    this.uncoveredInWeakerDegree = new LinkedHashSet<Tuple>();
    this.violations = new LinkedHashSet<Tuple>();
    this.cm = Checks.checknotnull(cm);
    this.covered = 0;
    if (this.degree == 1) {
      this.yetToBeCovered = new LinkedHashSet<Tuple>(factors.generateAllPossibleTuples(1));
      int c = 0;
      for (Factor each : factors) {
        c += each.levels.size();
      }
      this.initialSize = c;
    } else {
      ////
      // Any better way than this hack?
      final int[] c = new int[] { 0 };
      this.yetToBeCovered = new LinkedHashSet<Tuple>(factors.generateAllPossibleTuples(
          degree,
          new Utils.Predicate<Tuple>() {
            @Override
            public boolean apply(Tuple in) {
              c[0]++;
              for (Tuple each : subtuplesOf(in, degree - 1)) {
                ////
                // argument 'uncoveredInWeakerDegree` holds tuples not covered in
                // the parent of this object.
                // This statement will convert tuples in the set into one-degree-stronger,
                // which is suitable for this instance.
                if (uncoveredInWeakerDegree.contains(each)) {
                  Coverage.this.uncoveredInWeakerDegree.add(in);
                }
              }
              //noinspection EmptyCatchBlock
              try {
                if (!Coverage.this.cm.check(in)) {
                  Coverage.this.violations.add(in);
                }
              } catch (UndefinedSymbol undefinedSymbol) {
              }
              return true;
            }
          }
      ));
      this.initialSize = c[0];
    }
  }

  public Coverage processTestSuite(List<Tuple> testSuite) {
    for (Tuple eachTestCase : testSuite) {
      if (this.yetToBeCovered.isEmpty())
        break;
      this.processTestCase(eachTestCase);
    }
    this.state = State.PROCESSED;
    return createNext();
  }

  protected void processTestCase(Tuple testCase) {
    try {
      if (this.cm.check(testCase)) {
        for (Tuple each : subtuplesOf(testCase, this.degree)) {
          if (yetToBeCovered.remove(each)) {
            this.covered++;
          }
          if (yetToBeCovered.isEmpty())
            return;
        }
      }
    } catch (UndefinedSymbol undefinedSymbol) {
      ////
      // Actually, this will not happen because the test case given to this method
      // is a 'complete' one.
      throw Checks.wrap(undefinedSymbol);
    }
  }

  protected Set<Tuple> subtuplesOf(Tuple testCase, int strength) {
    return TupleUtils.subtuplesOf(testCase, strength);
  }

  protected Coverage createNext() {
    if (this.degree == factorSpace.size())
      return null;
    Set<Tuple> p = new LinkedHashSet<Tuple>();
    p.addAll(this.uncoveredInWeakerDegree);
    p.addAll(this.yetToBeCovered);
    return new Coverage(factorSpace, degree + 1, this.cm, p, this.report);
  }

  public static Coverage[] examineTestSuite(List<Tuple> testSuite, TestSpace testSpace, Report report) {
    Checks.checkcond(testSpace.getStrength() > 0);
    Coverage[] ret = new Coverage[testSpace.getStrength()];

    for (Coverage cur = new Coverage(testSpace.getFactorSpace(), 1, testSpace.cm, new LinkedHashSet<Tuple>(), report);
         cur != null && cur.degree <= testSpace.getStrength();
         cur = cur.createNext()) {
      cur.processTestSuite(testSuite);
      ret[cur.degree - 1] = cur;
    }

    return ret;
  }

  public static void examime(List<Tuple> testSuite, TestSpace testSpace, Report report) {
    Coverage[] coverages = examineTestSuite(testSuite, testSpace, report);
    for (Coverage each : coverages) {
      each.submit();
    }
  }

  public Set<Tuple> getUncoveredInWeakerDegree() {
    return uncoveredInWeakerDegree;
  }

  public Set<Tuple> getViolations() {
    return violations;
  }

  public int getCovered() {
    return covered;
  }

  public Set<Tuple> getYetToBeCovered() {
    return yetToBeCovered;
  }

  private enum State {
    NOT_PROCESSED,
    PROCESSED
  }

  public interface Report {
    void submit(Coverage coverage);
  }

  public void submit() {
    this.report.submit(this);
  }

  public static class TestSpace {
    private final Factors           factorSpace;
    private final int               strength;
    private final ConstraintManager cm;

    public TestSpace(Factors factorSpace, int strength, ConstraintManager cm) {
      this.factorSpace = factorSpace;
      this.strength = strength;
      this.cm = cm;
    }

    public Factors getFactorSpace() {
      return factorSpace;
    }

    public int getStrength() {
      return strength;
    }

    public ConstraintManager getCm() {
      return cm;
    }
  }
}
