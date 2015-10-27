package com.github.dakusui.jcunit.coverage;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;

import java.io.PrintStream;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class Coverage {
  final int        strength;
  final int        initialSize;
  final Factors    factorSpace;
  final Set<Tuple> uncovered;


  public Coverage(final Set<Tuple> uncovered, Factors factors, final int strength) {
    this.factorSpace = Checks.checknotnull(factors);
    Checks.checkcond(strength <= factors.size(), "strength=%s, factors.size=%s", strength, factors.size());
    Checks.checkcond(strength > 0);
    this.strength = strength;
    if (this.strength == 1) {
      Checks.checkcond(uncovered.size() == 0);
      this.uncovered = new LinkedHashSet<Tuple>(factors.generateAllPossibleTuples(1));
      int c = 0;
      for (Factor each : factors) {
        c += each.levels.size();
      }
      this.initialSize = c;
    } else {
      ////
      // Any better way than this hack?
      final int[] c = new int[1];
      c[0] = 0;
      this.uncovered = new LinkedHashSet<Tuple>(factors.generateAllPossibleTuples(
          strength,
          new Utils.Predicate<Tuple>() {
            @Override
            public boolean apply(Tuple in) {
              for (Tuple each : TupleUtils.subtuplesOf(in, strength - 1)) {
                System.out.println(each);
                c[0]++;
                if (uncovered.contains(each))
                  return false;
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
      uncovered.remove(each);
      if (uncovered.isEmpty())
        return;
    }
  }

  protected Coverage createNext() {
    if (this.strength == factorSpace.size())
      return null;
    if (this.strength == 1) {
      ////
      // Remove impossible levels
      Factors.Builder factorSpaceBuilder = new Factors.Builder();
      for (Factor eachFactor : factorSpace) {
        Factor.Builder factorBuilder = new Factor.Builder(eachFactor.name);
        for (Object eachLevel : eachFactor.levels) {
          if (!this.uncovered.contains(new Tuple.Builder().put(eachFactor.name, eachLevel).build())) {
            factorBuilder.addLevel(eachLevel);
          }
        }
        factorSpaceBuilder.add(factorBuilder.build());
      }
      return new Coverage(
          Collections.<Tuple>emptySet(),
          factorSpaceBuilder.build(),
          strength + 1);
    }
    return new Coverage(this.uncovered, factorSpace, strength + 1);
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
    stdout.printf("strength=%s %3s/%3s/%3s (not covered/covered/total) %n",
        strength,
        uncovered.size(),
        initialSize - uncovered.size(),
        initialSize
        );
    stdout.printf("--------------------------------------------------------------------------------%n");
    for (Tuple each : this.uncovered) {
      stdout.println(each);
    }
  }
}
