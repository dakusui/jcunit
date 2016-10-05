package com.github.dakusui.jcunit.plugins.caengines.ipo3;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.SystemProperties;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.utils.Utils.*;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.*;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.enumerateCartesianProduct;
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
 *            Pi and t -1 parameters among the first i – 1 parameters
 *     6.     // horizontal extension for parameter Pi
 *     7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
 *     8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
 *                ..., vi-1 , vi ) so that τ’ covers the most number of
 *                combinations of values in π
 *     9.         remove from π the combinations of values covered by τ’
 *     10.    }
 *     11.    // vertical extension for parameter P i
 *     12.    for (each combination σ in set π ) {
 *     13.      if (there exists a test that already covers σ ) {
 *     14.          remove σ from π
 *     15.      } else {
 *     16.          change an existing test, if possible, or otherwise add a new test
 *                  to cover σ and remove it from π
 *     17.      }
 *     18.    }
 *     19.  }
 *     20.  return ts;
 *    }
 *   See http://barbie.uta.edu/~fduan/ACTS/IPOG_%20A%20General%20Strategy%20for%20T-Way%20Software%20Testing.pdf
 * </pre>
 */
public class Ipo3 extends Ipo {
  static class GroupedFactor extends Factor {
    private static final int NUMBER_OF_RETRIES = 50;

    private final List<Factor> subfactors;

    private GroupedFactor(List<Factor> subfactors, List<Constraint> constraints, int strength) {
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
        Tuple next;
        //noinspection StatementWithEmptyBody
        while ((next = chooseNext(work, subtuples, strength)) == null) {
          ////
          // Could not choose that covers at least 1 sub tuple in a variable
          // subtuples. This can happen but very rare.
        }
        ret.add(next);
      }

      return ret;
    }

    private static Tuple chooseNext(List<Tuple> work, Set<Tuple> subtuples, int strength) {
      int bestIndex = -1;
      Set<Tuple> subtuplesCoveredByCurrentBestTuple = null;

      for (int i : randomizedIndexes(Math.min(work.size(), NUMBER_OF_RETRIES))) {
        Set<Tuple> overlap = Utils.overlap(TupleUtils.subtuplesOf(work.get(i), strength), subtuples);
        if (bestIndex == -1 || overlap.size() > subtuplesCoveredByCurrentBestTuple.size()) {
          bestIndex = i;
          subtuplesCoveredByCurrentBestTuple = overlap;
        }
      }
      if (bestIndex == -1) {
        return null;
      }
      subtuples.removeAll(subtuplesCoveredByCurrentBestTuple);
      return work.remove(bestIndex);
    }

    private static int[] randomizedIndexes(int num) {
      List<Integer> work = new ArrayList<Integer>(num);
      for (int i = 0; i < num; i++) {
        work.add(i);
      }
      Collections.shuffle(work, new Random(SystemProperties.randomSeed()));
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
    if (factors.size() < this.strength) {
      // FIXME: 10/6/16 In case factors is smaller than strength, nothing can be done. Or
      //        should return a covering array whose strength is factors.size()?
      return null;
    } else if (factors.size() == this.strength) {
      return new Result(
          Collections.<Tuple>emptyList(),
          generateAllPossibleTuples(factors.asFactorList(), filterViolations(constraintChecker)));
    }
    /*
     *   Algorithm: IPOG-Test (int t , ParameterSet ps ) {
     *     1.  initialize test set ts to be an empty set
     *     2.  denote the parameters in ps , in an arbitrary order, as P1 , P2, ...,
     *         and Pn
     *     3.  add into test set ts a test for each combination of values of the first
     *         t parameters
     */
    List<Tuple> ts = generateAllPossibleTuples(factors.asFactorList(), filterViolations(constraintChecker));
    /*
     *     4.  for (int i = t + 1 ; i ≤ n ; i ++ ){
     */
    /*
     *
     *     5.     let π be the set of t -way combinations of values involving parameter
     *            Pi and t -1 parameters among the first i – 1 parameters
     *     6.     // horizontal extension for parameter Pi
     *     7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
     *     8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
     *                ..., vi-1 , vi ) so that τ’ covers the most number of
     *                combinations of values in π
     *     9.         remove from π the combinations of values covered by τ’
     *     10.    }
     *     11.    // vertical extension for parameter P i
     *     12.    for (each combination σ in set π ) {
     *     13.      if (there exists a test that already covers σ ) {
     *     14.          remove σ from π
     *     15.      } else {
     *     16.          change an existing test, if possible, or otherwise add a new test
     *                  to cover σ and remove it from π
     *     17.      }
     *     18.    }
     *     19.  }
     *     20.  return ts;
     *    }
     */
    return null;
  }

  private static Predicate<Tuple> filterViolations(final ConstraintChecker constraintChecker) {
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
      public GroupedFactor apply(List<Factor> in) {
        // FIXME: 10/6/16
        return null;
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

  private List<List<String>> groupFactorNames(List<Constraint> constraints) {
    List<List<String>> work = transform(constraints, new Form<Constraint, List<String>>() {
      @Override
      public List<String> apply(Constraint in) {
        return in.getFactorNamesInUse();
      }
    });
    List<List<String>> ret = new LinkedList<List<String>>();
    for (List<String> outer : work) {
      List<String> overlapping = null;
      for (List<String> inner : ret) {
        if (overlaps(outer, inner)) {
          overlapping = inner;
          break;
        }
      }
      if (overlapping == null) {
        overlapping = new LinkedList<String>();
        ret.add(overlapping);
      }
      merge(overlapping, outer);
    }
    return ret;
  }

  private static void merge(List<String> a, List<String> b) {
    for (String each : b) {
      if (!a.contains(each)) {
        a.add(each);
      }
    }
  }

  private static boolean overlaps(List<String> a, List<String> b) {
    for (String each : a) {
      if (b.contains(each))
        return true;
    }
    return false;
  }
}
