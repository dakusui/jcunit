package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;

import java.io.PrintStream;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Coverage {
  final   int        strength;
  final   int        initialSize;
  final   Factors    factorSpace;
  final   Set<Tuple> uncovered;
  final   Set<Tuple> prohibited;
  private int        covered;

  /**
   * Creates an object of this class.
   *
   * @param prohibited_ Tuples whose strength are less than {@code strength} and known impossible.
   * @param factors     A factor space where test suite's coverage is examined.
   * @param strength    Strength (number of attributes involved).
   */
  public Coverage(final Set<Tuple> prohibited_, Factors factors, final int strength) {
    Checks.checkcond(strength > 0);
    Checks.checkcond(strength <= factors.size(), "strength=%s, factors.size=%s", strength, factors.size());
    this.factorSpace = Checks.checknotnull(factors);
    this.strength = strength;
    this.prohibited = new LinkedHashSet<Tuple>();
    this.covered =0;
    if (this.strength == 1) {
      this.uncovered = new LinkedHashSet<Tuple>(factors.generateAllPossibleTuples(1));
      int c = 0;
      for (Factor each : factors) {
        c += each.levels.size();
      }
      this.initialSize = c;
    } else {
      ////
      // Any better way than this hack?
      final int[] c = new int[] { 0 };
      this.uncovered = new LinkedHashSet<Tuple>(factors.generateAllPossibleTuples(
          strength,
          new Utils.Predicate<Tuple>() {
            @Override
            public boolean apply(Tuple in) {
              c[0]++;
              for (Tuple each : TupleUtils.subtuplesOf(in, strength - 1)) {
                if (prohibited_.contains(each)) {
                  Coverage.this.prohibited.add(in);
                  return true;
                }
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
      if (this.uncovered.isEmpty())
        break;
      this.processTestCase(eachTestCase);
    }
    return createNext();
  }

  protected void processTestCase(Tuple testCase) {
    for (Tuple each : TupleUtils.subtuplesOf(testCase, this.strength)) {
      if (uncovered.remove(each)) {
        this.covered++;
      }
      if (uncovered.isEmpty())
        return;
    }
  }

  protected Coverage createNext() {
    if (this.strength == factorSpace.size())
      return null;
    Set<Tuple> p = new LinkedHashSet<Tuple>();
    p.addAll(this.prohibited);
    p.addAll(this.uncovered);
    return new Coverage(p, factorSpace, strength + 1);
  }

  public static Coverage[] examineTestSuite(List<Tuple> testSuite, Factors factorSpace, int strength) {
    Checks.checkcond(strength > 0);
    Coverage[] ret = new Coverage[strength];

    for (Coverage cur = new Coverage(new LinkedHashSet<Tuple>(), factorSpace, 1);
         cur != null && cur.strength <= strength;
         cur = cur.createNext()) {
      cur.processTestSuite(testSuite);
      ret[cur.strength - 1] = cur;
    }

    return ret;
  }

  public void printReport(PrintStream stdout) {
    Checks.checknotnull(stdout);
    stdout.printf("================================================================================%n");
    stdout.printf("RESULT: %3s/%3s/%3s/%3s (covered/not covered/prohibited/total) strength=%s%n",
        covered,
        uncovered.size(),
        prohibited.size(),
        initialSize,
        strength
    );
    stdout.printf("--------------------------------------------------------------------------------%n");
    for (Tuple each : this.uncovered) {
      stdout.printf(
          "%1s:%s%n",
          prohibited.contains(each) ? "P" : "N",
          each);
    }
    stdout.println();
  }
}
