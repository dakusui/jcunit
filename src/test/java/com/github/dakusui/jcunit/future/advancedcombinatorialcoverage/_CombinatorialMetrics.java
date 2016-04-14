package com.github.dakusui.jcunit.future.advancedcombinatorialcoverage;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 * A class to measure t-way coverage.
 */
public class _CombinatorialMetrics {
  public final  int                 degree;
  public final  int                 initialSize;
  public final  Factors             factors;
  private final Set<Tuple>          yetToBeCovered;
  private final Set<Tuple>          uncoveredInWeakerDegree;
  private final Set<Tuple>          violations;
  public final  ConstraintChecker   cm;
  public final  CombinatorialReport report;
  private       int                 covered;
  protected State state = State.NOT_PROCESSED;

  /**
   * Creates an object of this class.
   *
   * @param uncoveredInWeakerDegree Tuples whose degree are less than {@code degree} and known impossible.
   * @param factors                 A factor space where test suite's coverage is examined.
   * @param degree                  Strength (number of attributes involved).
   */
  public _CombinatorialMetrics(
      Factors factors,
      final int degree,
      ConstraintChecker cm,
      final Set<Tuple> uncoveredInWeakerDegree,
      CombinatorialReport report) {
    Checks.checkcond(degree > 0);
    Checks.checkcond(degree <= factors.size(), "degree=%s, factors.size=%s", degree, factors.size());
    this.factors = Checks.checknotnull(factors);
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
                  _CombinatorialMetrics.this.uncoveredInWeakerDegree.add(in);
                }
              }
              //noinspection EmptyCatchBlock
              try {
                if (!_CombinatorialMetrics.this.cm.check(in)) {
                  _CombinatorialMetrics.this.violations.add(in);
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

  public _CombinatorialMetrics processTestSuite(List<Tuple> testSuite) {
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

  protected _CombinatorialMetrics createNext() {
    if (this.degree == factors.size())
      return null;
    Set<Tuple> p = new LinkedHashSet<Tuple>();
    p.addAll(this.uncoveredInWeakerDegree);
    p.addAll(this.yetToBeCovered);
    return new _CombinatorialMetrics(factors, degree + 1, this.cm, p, this.report);
  }

  public static void examine(List<Tuple> testSuite, TestSpace testSpace, CombinatorialReport report) {
    _CombinatorialMetrics[] combinatorialMetricses = examineTestSuite(testSuite, testSpace, report);
    for (_CombinatorialMetrics each : combinatorialMetricses) {
      each.report.submit(each);
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

  private static _CombinatorialMetrics[] examineTestSuite(List<Tuple> testSuite, TestSpace testSpace, CombinatorialReport report) {
    Checks.checkcond(testSpace.getStrength() > 0);
    _CombinatorialMetrics[] ret = new _CombinatorialMetrics[testSpace.getStrength()];

    for (_CombinatorialMetrics cur = new _CombinatorialMetrics(testSpace.getFactorSpace(), 1, testSpace.cm, new LinkedHashSet<Tuple>(), report);
         cur != null && cur.degree <= testSpace.getStrength();
         cur = cur.createNext()) {
      cur.processTestSuite(testSuite);
      ret[cur.degree - 1] = cur;
    }

    return ret;
  }

  private enum State {
    NOT_PROCESSED,
    PROCESSED
  }

  public interface CombinatorialReport {
    void submit(_CombinatorialMetrics combinatorialMetrics);

    class Printer implements CombinatorialReport {
      private final PrintStream out;

      public Printer(PrintStream out) {
        this.out = Checks.checknotnull(out);
      }

      @Override
      public void submit(_CombinatorialMetrics combinatorialMetrics) {
        out.printf("STRENGTH=%2s: %3s/%3s/%3s/%3s/%3s (uncovered in weaker degree/violations/covered/yet to be covered/total)%n",
            combinatorialMetrics.degree,
            combinatorialMetrics.getUncoveredInWeakerDegree().size(),
            combinatorialMetrics.getViolations().size(),
            combinatorialMetrics.getCovered(),
            combinatorialMetrics.getYetToBeCovered().size(),
            combinatorialMetrics.initialSize
        );
        for (Tuple each : combinatorialMetrics.getYetToBeCovered()) {
          out.printf(
              "%1s:%1s:%s%n",
              combinatorialMetrics.getViolations().contains(each)
                  ? "V" : " ",
              combinatorialMetrics.getUncoveredInWeakerDegree().contains(each)
                  ? "W" : " ",
              each);
        }
        out.println();
      }
    }
  }

  public static class TestSpace {
    private final Factors           factorSpace;
    private final int               strength;
    private final ConstraintChecker cm;

    public TestSpace(Factors factorSpace, int strength, ConstraintChecker cm) {
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

    public ConstraintChecker getCm() {
      return cm;
    }
  }
}
