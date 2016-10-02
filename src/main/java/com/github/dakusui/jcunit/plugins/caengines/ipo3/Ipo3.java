package com.github.dakusui.jcunit.plugins.caengines.ipo3;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.utils.Utils.*;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.*;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.enumerateCartesianProduct;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit.core.utils.Checks.wraptesterror;
import static com.github.dakusui.jcunit.core.utils.Utils.*;

/**
 * <pre>
 *   Algorithm: IPOG-Test (int t , ParameterSet ps ) {
 *     1.  initialize test set ts to be an empty set
 *     2.  denote the parameters in ps , in an arbitrary order, as P1 , P2, ...,
 *         and Pn
 *     3.  add into test set ts a test for each combination of values of the first
 *         t parameters
 *     4.  for (int i = t + 1 ; i ≤ n ; i ++ ){
 *     5.     let π be the set of t -way combinations of values involving parameter
 *             P i and t -1 parameters among the first i – 1 parameters
 *     6.     // horizontal extension for parameter Pi
 *     7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
 *     8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
 *                ..., vi-1 , vi ) so that τ’ covers the most number of
 *                combinations of values in π
 *     9.         remove from π the combinations of values covered by τ’
 *     10.    }
 *     11.    // vertical extension for parameter P i
 *     12. for (each combination σ in set π ) {
 *     13. if (there exists a test that already covers σ ) {
 *     14.          remove σ from π
 *     15.      } else {
 *     16.          change an existing test, if possible, or otherwise add a new test
 *                  to cover σ and remove it from π
 *     17.      }
 *     18.   }
 *     19.}
 *     20.return ts;
 *    }
 *   See http://barbie.uta.edu/~fduan/ACTS/IPOG_%20A%20General%20Strategy%20for%20T-Way%20Software%20Testing.pdf
 * </pre>
 */
public class Ipo3 extends Ipo {
  static class AggregatedFactor extends Factor {

    private final List<Factor> subfactors;

    private AggregatedFactor(List<Factor> subfactors, List<Constraint> constraints, int strength) {
      super(composeFactorName(subfactors), optimize(composeLevels(subfactors, constraints), strength));
      this.subfactors = Collections.unmodifiableList(subfactors);
    }

    private static List<Tuple> composeLevels(List<Factor> subfactors, final List<Constraint> constraints) {
      return filter(
          enumerateCartesianProduct(new Tuple.Impl(), subfactors.toArray(new Factor[constraints.size()])),
          new Predicate<Tuple>() {
            @Override
            public boolean apply(Tuple in) {
              for (Constraint each : constraints) {
                try {
                  if (!each.check(in)) {
                    return false;
                  }
                } catch (UndefinedSymbol undefinedSymbol) {
                  throw wraptesterror(undefinedSymbol,
                      "A constraint '%s' threw '%s' (missing %s). May be it is not annotated appropriately.",
                      each, undefinedSymbol, undefinedSymbol.missingSymbols);
                }
              }
              return true;
            }
          });
    }

    private static List<Tuple> optimize(List<Tuple> tuples, int strength) {
      Set<Tuple> subtuples = subtuplesCoveredBy(tuples, strength);
      List<Tuple> ret = new LinkedList<Tuple>();
      List<Tuple> work = new ArrayList<Tuple>(tuples);
      while (!subtuples.isEmpty()) {
        ret.add(chooseNext(work, subtuples, strength));
      }
      return ret;
    }

    private static Tuple chooseNext(List<Tuple> work, Set<Tuple> subtuples, int strength) {
      int retries = 50;
      int bestIndex = -1;
      Set<Tuple> subtuplesCoveredByCurrentBestTuple = null;

      for (int i: randomizedIndexes(Math.min(work.size(), retries))) {
        Set<Tuple> overlap = Utils.overlap(TupleUtils.subtuplesOf(work.get(i), strength), subtuples);
        if (bestIndex == -1 || overlap.size() > subtuplesCoveredByCurrentBestTuple.size()) {
          bestIndex = i;
          subtuplesCoveredByCurrentBestTuple = overlap;
        }
      }
      subtuples.removeAll(checknotnull(subtuplesCoveredByCurrentBestTuple));
      return checknotnull(work.remove(bestIndex));
    }

    private static int[] randomizedIndexes(int num) {
      List<Integer> work = new ArrayList<Integer>(num);
      for (int i = 0 ; i < num; i++) {
        work.add(i);
      }
      Collections.shuffle(work, new Random(0));
      int[] ret = new int[num];
      for (int i = 0; i < num; i++) {
        ret[i] = work.get(i);
      }
      return ret;
    }

    private static Set<Tuple> subtuplesCoveredBy(Collection<Tuple> tuples, int strength) {
      Set<Tuple> ret = new HashSet<Tuple>();
      for (Tuple each : tuples) {
        ret.addAll(TupleUtils.subtuplesOf(each, strength));
      }
      return ret;
    }

    private static String composeFactorName(List<Factor> subfactors) {
      return StringUtils.join("+", transform(subfactors, new Utils.Form<Factor, String>() {
        @Override
        public String apply(Factor in) {
          return in.name;
        }
      }));
    }

    List<Factor> getSubfactors() {
      return this.subfactors;
    }
  }

  public Ipo3(int strength, ConstraintChecker constraintChecker, Factors factors) {
    super(strength, constraintChecker, factors);
  }

  @Override
  public Result ipo() {
    if (factors.size() == this.strength) {
      return new Result(
          Collections.<Tuple>emptyList(),
          generateAllPossibleTuples(factors.asFactorList(), filterViolations(constraintChecker)));
    }
    ////
    // Group factors used by constraints so that each group disjoints each other.
    LinkedHashMap<Factor, List<Factor>> aggregatedFactors = new LinkedHashMap<Factor, List<Factor>>();
    for (List<Factor> eachFactorGroup : transformLazily(this.groupFactorNames(constraintChecker.getConstraints()), transformFactorNamesToFactors())) {
      aggregatedFactors.put(
          aggregateFactors(constraintChecker).apply(eachFactorGroup),
          eachFactorGroup);
    }
    return null;
  }

  protected static Predicate<Tuple> filterViolations(final ConstraintChecker constraintChecker) {
    return new Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        try {
          return constraintChecker.check(in);
        } catch (UndefinedSymbol undefinedSymbol) {
          // This should not happen through usual usage scenario.
          throw Checks.wrap(undefinedSymbol);
        }
      }
    };
  }

  private static Utils.Form<List<Factor>, Factor> aggregateFactors(final ConstraintChecker constraintChecker) {
    return new Utils.Form<List<Factor>, Factor>() {
      /**
       *
       * @param in Already grouped factors
       */
      @Override
      public Factor apply(List<Factor> in) {
        String factorName = StringUtils.join("+", in);
        Factor.Builder builder = new Factor.Builder(factorName);
        for (Tuple eachTuple : optimizeSubtuples(generateAllPossibleTuples(in, filterViolations(constraintChecker)))) {
          builder.addLevel(eachTuple);
        }
        return builder.build();
      }

      private List<Tuple> optimizeSubtuples(List<Tuple> tuples) {
        ////
        // fixme: Remove subtuples that do not contribute to combinatorial coverage.
        return tuples;
      }
    };
  }

  Utils.Form<List<String>, List<Factor>> transformFactorNamesToFactors() {
    return new Utils.Form<List<String>, List<Factor>>() {
      @Override
      public List<Factor> apply(List<String> in) {
        return transformLazily(in, new Utils.Form<String, Factor>() {
          @Override
          public Factor apply(String in) {
            return factors.get(in);
          }
        });
      }
    };
  }

  List<List<String>> groupFactorNames(List<Constraint> constraints) {
    ////
    // fixme: Implement this method.
    List<Set<String>> ret = new LinkedList<Set<String>>();
    for (Constraint each : constraints) {
      each.getFactorNamesInUse();
    }
    return null;
  }
}
