package com.github.dakusui.jcunit.plugins.caengines.ipo3;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.utils.Utils.Form;
import com.github.dakusui.jcunit.core.utils.Utils.Predicate;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;

import java.util.*;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.enumerateCartesianProduct;
import static com.github.dakusui.jcunit.core.tuples.TupleUtils.sortStably;
import static com.github.dakusui.jcunit.core.utils.Checks.*;
import static com.github.dakusui.jcunit.core.utils.Utils.filter;
import static com.github.dakusui.jcunit.core.utils.Utils.transform;

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
public class IpoGc extends Ipo {

  public IpoGc(int strength, ConstraintChecker constraintChecker, Factors factors) {
    super(strength, constraintChecker, factors);
  }

  @Override
  public Result ipo() {
    Checks.checkcond(this.factors.size() >= this.strength);
    if (this.factors.size() == this.strength) {
      return new Result(
          generateAllPossibleTuples(this.factors.asFactorList(), isViolating(constraintChecker.getConstraints())),
          Collections.<Tuple>emptyList()
      );
    }
    List<Factor> allFactors = arrangeFactors(this.factors, this.constraintChecker.getConstraints(), this.strength);
    List<Factor> processedFactors = new LinkedList<Factor>(allFactors.subList(0, this.strength));
    /*
     *   Algorithm: IPOG-Test (int t , ParameterSet ps ) {
     *     1.  initialize test set ts to be an empty set
     *     2.  denote the parameters in ps , in an arbitrary order, as P1 , P2, ...,
     *         and Pn
     *     3.  add into test set ts a test for each combination of values of the first
     *         t parameters
     */
    List<Tuple> ts = generateAllPossibleTuples(processedFactors, isViolating(filter(constraintChecker.getConstraints(), isRelated(processedFactors))));
    /*
     *     4.  for (int i = t + 1 ; i ≤ n ; i ++ ){
     *         * t; strength
     *         * 0-origin
     */
    int t = this.strength;
    int n = allFactors.size();
    List<Tuple> π;
    for (int i = t + 1; i <= n; i++) {
      /*   5.     let π be the set of t-way combinations of values involving parameter
       *          Pi and t -1 parameters among the first i – 1 parameters
       */
      Factor Pi = allFactors.get(i - 1);
      π = prepare_π(processedFactors, Pi, t);
      /*    6.     // horizontal extension for parameter Pi
       *    7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
       */
      for (Tuple τ : ts) {
        /*  8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
         *             ..., vi-1 , vi ) so that τ’ covers the most number of
         *             combinations of values in π
         */
        Object vi = chooseLevelThatCoversMostTuples(Pi, τ, π);
        update_τ_withChosenLevel(Pi, τ, vi);
        /*  9.         remove from π the combinations of values covered by τ’
         */
        π.removeAll(TupleUtils.subtuplesOf(τ, t));
      }

      /* 11.    // vertical extension for parameter P i
       * 12.    for (each combination σ in set π ) {
       */
      for (Tuple σ : new LinkedList<Tuple>(π)) {
        /* 13.      if (there exists a test that already covers σ ) {
         * 14.          remove σ from π
         * 15.      } else {
         * 16.        change an existing test, if possible, or otherwise add a new test
         *            to cover σ and remove it from π
         * 17.      }
         */
        if (containsTestThatCovers(ts, σ)) {
          π.remove(σ);
        } else {
          Tuple chosenTest = chooseTestToCover(ts, σ);
          if (chosenTest == null) {
            chosenTest = createTupleFrom(processedFactors, σ, DontCare);
            ts.add(chosenTest);
          } else {
            changeTestToCover(chosenTest, σ);
            π.remove(σ);
          }
        }
      }
      ts = transform(ts, fillout(processedFactors));
    }
    /*     20.  return ts;
     */
    return new Result(ts, Collections.<Tuple>emptyList());
  }

  private Form<Tuple, Tuple> fillout(List<Factor> factorList) {
    final Factors factors = new Factors(factorList);
    return new Form<Tuple, Tuple>() {
      int i = 0;

      @Override
      public Tuple apply(Tuple in) {
        for (Map.Entry<String, Object> each : in.entrySet()) {
          if (each.getValue() == DontCare) {
            List levels = checknotnull(factors.get(each.getKey())).levels;
            each.setValue(levels.get(i++ % levels.size()));
          }
        }
        return in;
      }
    };
  }

  private void update_τ_withChosenLevel(Factor pi, Tuple τ, Object vi) {
    if (pi instanceof GroupedFactor) {
      τ.putAll((Tuple) vi);
    } else {
      τ.put(pi.name, vi);
    }
  }

  private static Predicate<Constraint> isRelated(final List<Factor> factors) {
    return new Predicate<Constraint>() {
      @Override
      public boolean apply(Constraint in) {
        return overlaps(in.getFactorNamesInUse(), transform(factors, new Form<Factor, String>() {
          @Override
          public String apply(Factor in) {
            return in.name;
          }
        }));
      }
    };
  }

  /**
   * <pre>
   * 16. change an existing test, if possible, or otherwise add a new test
   *     to cover σ
   * </pre>
   */
  private void changeTestToCover(Tuple chosenTest, Tuple σ) {
    chosenTest.putAll(σ);
  }

  /**
   * <pre>
   * 16. change an existing test, if possible, or otherwise add a new test
   *     to cover σ
   * </pre>
   */
  private Tuple createTupleFrom(List<Factor> processedFactors, Tuple σ, Object value) {
    Tuple ret = σ.cloneTuple();
    for (Factor each : processedFactors) {
      if (each instanceof GroupedFactor) {
        for (Factor eachSubFactor : ((GroupedFactor) each).getSubfactors()) {
          ret.put(eachSubFactor.name, value);
        }
      } else {
        ret.put(each.name, value);
      }
    }
    return ret;
  }


  /**
   * Returns {@code null} if no test in ts can cover σ.
   * <pre>
   * 16.        change an existing test, if possible, or otherwise add a new test
   *            to cover σ and remove it from π
   * </pre>
   * σ is a partial tuple.
   * ts is a list of partial test cases,  each of which has same keys.
   * We already know that ts doesn't contain any test that covers σ.
   * This method chooses a test from ts by
   */
  private Tuple chooseTestToCover(List<Tuple> ts, Tuple σ) {
    List<Tuple> found = new LinkedList<Tuple>();
    for (Tuple each : ts) {
      boolean canBeChanged = true;
      for (String eachKeyOfσ : σ.keySet()) {
        if (!DontCare.equals(each.get(eachKeyOfσ))) {
          canBeChanged = false;
          break;
        }
      }
      if (canBeChanged) {
        found.add(each);
      }
    }
    return found.isEmpty() ?
        null :
        found.get(0);
  }

  /*
   *  13.      if (there exists a test that already covers σ ) {
   */
  private boolean containsTestThatCovers(List<Tuple> ts, Tuple σ) {
    for (Tuple each : ts) {
      if (TupleUtils.isSubtupleOf(σ, each)) {
        return true;
      }
    }
    return false;
  }

  /*
   *  8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
   *             ..., vi-1 , vi ) so that τ’ covers the most number of
   *             combinations of values in π
   */
  private Object chooseLevelThatCoversMostTuples(Factor fi, Tuple τ, List<Tuple> π) {
    int numCoveredTuples = -1;
    Object ret = null;
    for (Object v : fi) {
      Tuple τ$ = τ.cloneTuple();
      if (fi instanceof GroupedFactor) {
        τ$.putAll((Tuple) v);
      } else {
        update_τ_withChosenLevel(fi, τ$, v);
      }
      int c = countCoveredTuplesBy(τ$, π);
      if (c > numCoveredTuples) {
        numCoveredTuples = c;
        ret = v;
      }
    }
    checkcond(numCoveredTuples >= 0);
    return ret;
  }

  private int countCoveredTuplesBy(Tuple τ$, final List<Tuple> π) {
    return filter(TupleUtils.subtuplesOf(τ$, this.strength), new Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        return π.contains(in);
      }
    }).size();
  }

  /*
   *  5.     let π be the set of t-way combinations of values involving parameter
   *         Pi and t -1 parameters among the first i – 1 parameters
   */
  private List<Tuple> prepare_π(List<Factor> processedFactors, Factor factor, int strength) {
    List<Tuple> ret = new LinkedList<Tuple>();
    if (factor instanceof GroupedFactor) {
      GroupedFactor coveringArray = (GroupedFactor) factor;
      for (int i = 1; i < strength; i++) {
        int j = strength - i;
        assert i + j == strength;

        List<Tuple> leftSide = new Factors(processedFactors).generateAllPossibleTuples(i);
        List<Tuple> rightSide = coveringArray.allPossibleTuples(j);
        for (Tuple right : rightSide) {
          for (Tuple left : leftSide) {
            Tuple cur = right.cloneTuple();
            cur.putAll(left);
            ret.add(cur);
          }
        }
        processedFactors.addAll(coveringArray.getSubfactors());
        ret = sortStably(ret, processedFactors);
      }
    } else {
      List<Tuple> leftSide = new Factors(processedFactors).generateAllPossibleTuples(strength - 1);
      for (Object v : factor) {
        for (Tuple each : leftSide) {
          each = each.cloneTuple();
          each.put(factor.name, v);
          ret.add(each);
        }
      }
      processedFactors.add(factor);
      ret = sortStably(ret, processedFactors);
    }
    return ret;
  }

  private static Predicate<Tuple> isViolating(
      final List<Constraint> constraints) {
    return new Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple in) {
        try {
          for (Constraint each : constraints) {
            if (!each.check(in)) {
              return false;
            }
          }
          return true;
        } catch (UndefinedSymbol undefinedSymbol) {
          // This should not happen through usual usage scenario.
          throw Checks.wrap(undefinedSymbol);
        }
      }
    };
  }

  private List<Factor> arrangeFactors(final Factors factors, final List<Constraint> constraints, final int strength) {
    final List<Factor> ret = new ArrayList<Factor>(factors.asFactorList());
    List<GroupedFactor> groupedFactors = transform(groupFactorNamesUsedByConstraints(constraints),
        new Form<List<String>, GroupedFactor>() {
          List<Factor> subfactors;

          @Override
          public GroupedFactor apply(List<String> in) {
            return new GroupedFactor(subfactors = transform(in, new Form<String, Factor>() {
              @Override
              public Factor apply(String in) {
                Factor factor = factors.get(in);
                ret.remove(factor);
                return factor;
              }
            }), Utils.filter(constraints, isRelated(subfactors)), strength);
          }
        }
    );
    ret.addAll(groupedFactors);
    return ret;
  }

  private List<List<String>> groupFactorNamesUsedByConstraints
      (List<Constraint> constraints) {
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

  static class GroupedFactor extends Factor {
    private static final int NUMBER_OF_RETRIES = 50;

    private final List<Factor> subfactors;

    GroupedFactor(List<Factor> subfactors, List<Constraint> constraints, int strength) {
      super(
          composeFactorName(subfactors),
          sortStably(optimize(composeLevels(subfactors, constraints), strength), subfactors));
      this.subfactors = Collections.unmodifiableList(subfactors);
    }

    List<Tuple> allPossibleTuples(int strength) {
      Set<Tuple> work = new LinkedHashSet<Tuple>();
      for (Object each : this.levels) {
        work.addAll(TupleUtils.subtuplesOf((Tuple) each, strength));
      }
      return sortStably(new ArrayList<Tuple>(work), getSubfactors());
    }

    private static List<Tuple> composeLevels(final List<Factor> subfactors, final List<Constraint> constraints) {
      return filter(enumerateCartesianProduct(new Tuple.Impl(), subfactors.toArray(new Factor[subfactors.size()])),
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
                      "A constraint '%s' threw '%s' (missing %s). Maybe it is not annotated appropriately.",
                      each, undefinedSymbol, undefinedSymbol.missingSymbols);
                }
              }
              return true;
            }
          });
    }

    private static List<Tuple> optimize(List<Tuple> tuples, int strength) {
      if (tuples.isEmpty()) {
        return tuples;
      }
      strength = Math.min(strength, tuples.get(0).size());
      final int finalStrength = strength;
      return Utils.filter(tuples, new Predicate<Tuple>() {
        Set<Tuple> alreadyCovered = new HashSet<Tuple>();
        @Override
        public boolean apply(Tuple in) {
          Set<Tuple> currentSubtuples =TupleUtils.subtuplesOf(in, finalStrength);
          if (!alreadyCovered.containsAll(currentSubtuples)) {
            alreadyCovered.addAll(currentSubtuples);
            return true;
          }
          return false;
        }
      });
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
}
