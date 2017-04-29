package com.github.dakusui.jcunit.plugins.caengines.ipogc;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.utils.Utils.Form;
import com.github.dakusui.jcunit.core.utils.Utils.Predicate;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;

import java.util.*;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
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
          generateAllPossibleTuples(this.factors.asFactorList(), isSatisfying(constraintChecker.getConstraints())),
          Collections.<Tuple>emptyList()
      );
    }
    List<Factor> allFactors = arrangeFactors(this.factors, this.constraintChecker.getConstraints(), this.strength);
    if (allFactors.size() < this.strength) {
      return new Result(
          generateAllPossibleTuples(this.factors.asFactorList(), isSatisfying(constraintChecker.getConstraints())),
          Collections.<Tuple>emptyList()
      );
    }

    List<Factor> processedFactors = new LinkedList<Factor>(allFactors.subList(0, this.strength));
    /*
     *   Algorithm: IPOG-Test (int t , ParameterSet ps ) {
     *     1.  initialize test set ts to be an empty set
     *     2.  denote the parameters in ps , in an arbitrary order, as P1 , P2, ...,
     *         and Pn
     *     3.  add into test set ts a test for each combination of values of the first
     *         t parameters
     */
    List<Tuple> ts = transform(
        generateAllPossibleTuples(
            processedFactors,
            isSatisfying(filter(constraintChecker.getConstraints(), isRelated(processedFactors)))),
        expandGroupedFactors(processedFactors)
    );

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
          Tuple chosenTest = chooseTestToCoverGivenTuple(processedFactors, ts, σ);
          if (chosenTest == null) {
            chosenTest = createTupleFrom(processedFactors, σ);
            ts.add(chosenTest);
          }
          modifyTestToCover(processedFactors, chosenTest, σ);
          π.remove(σ);
        }
      }
      ts = Utils.dedup(transform(ts, fillout(processedFactors)));
    }
    /*     20.  return ts;
     */
    return new Result(TestCaseUtils.optimize(TestCaseUtils.unique(ts), strength), Collections.<Tuple>emptyList());
  }

  /*
   *  5.     let π be the set of t-way combinations of values involving parameter
   *         Pi and t -1 parameters among the first i – 1 parameters
   */
  private List<Tuple> prepare_π(List<Factor> processedFactors, final Factor factor, int strength) {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (int i = 0; i <= strength; i++) {
      int j = strength - i;
      assert i + j == strength;

      List<Tuple> rightSide;
      if (factor instanceof GroupedFactor) {
        GroupedFactor coveringArray = (GroupedFactor) factor;
        if (j > coveringArray.getSubfactors().size())
          continue;
        rightSide = coveringArray.allPossibleTuples(j);
      } else {
        if (j != 1)
          continue;
        rightSide = transform(factor.levels, new Form<Object, Tuple>() {
          @Override
          public Tuple apply(Object in) {
            return new Tuple.Builder().put(factor.name, in).build();
          }
        });
      }
      List<Tuple> leftSide = transform(new Factors(processedFactors).generateAllPossibleTuples(i),
          expandGroupedFactors(processedFactors));
      for (Tuple right : rightSide) {
        for (Tuple left : leftSide) {
          Tuple cur = right.cloneTuple();
          cur.putAll(left);
          ret.add(cur);
        }
      }
    }
    processedFactors.add(factor);
    return ret;
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

  /**
   * Chooses a test from {@code ts} to cover {@code σ}.
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
  private Tuple chooseTestToCoverGivenTuple(final List<Factor> factors, List<Tuple> ts, final Tuple σ) {
    Predicate<Tuple> matches = new Predicate<Tuple>() {
      @Override
      public boolean apply(Tuple current) {
        boolean ret = true;
        for (Factor each : figureOutInvolvedFactors(factors, σ)) {
          if (each instanceof GroupedFactor) {
            for (String eachSubFactorName : ((GroupedFactor) each).getSubfactorNames()) {
              Object currentLevel = current.get(eachSubFactorName);
              if (!(com.github.dakusui.jcunit8.core.Utils.DontCare.equals(currentLevel) || Utils.eq(currentLevel, σ.get(eachSubFactorName)))) {
                ret = false;
              }
            }
          } else {
            Object currentLevel = current.get(each.name);
            if (!(com.github.dakusui.jcunit8.core.Utils.DontCare.equals(currentLevel) || Utils.eq(currentLevel, σ.get(each.name)))) {
              ret = false;
            }
          }
        }
        return ret;
      }
    };
    List<Tuple> found = filter(ts, matches);
    return found.isEmpty() ?
        null :
        found.get(0);
  }

  /**
   * <pre>
   * 16. change an existing test, if possible, or otherwise add a new test
   *     to cover σ
   * </pre>
   */
  private void modifyTestToCover(List<Factor> factors, Tuple chosenTest, Tuple σ) {
    // simple 'chosenTest.putAll(σ)' doesn't work because σ can contain values
    // under GroupedFactor, whose values picked up at once rather than one by one.
    for (Factor each : figureOutInvolvedFactors(factors, σ)) {
      if (each instanceof GroupedFactor) {
        chosenTest.putAll(chooseLevelFromGroupedFactor(σ, (GroupedFactor) each));
      } else {
        chosenTest.put(each.name, σ.get(each.name));
      }
    }
  }

  /**
   * <pre>
   * 16. change an existing test, if possible, or otherwise add a new test
   *     to cover σ
   * </pre>
   */
  private Tuple createTupleFrom(List<Factor> processedFactors, Tuple σ) {
    Tuple.Builder builder = new Tuple.Builder();
    for (Factor each : processedFactors) {
      if (each instanceof GroupedFactor) {
        for (Factor eachSubFactor : ((GroupedFactor) each).getSubfactors()) {
          builder.put(eachSubFactor.name, com.github.dakusui.jcunit8.core.Utils.DontCare);
        }
      } else {
        builder.put(each.name, com.github.dakusui.jcunit8.core.Utils.DontCare);
      }
    }
    builder.putAll(σ);
    return builder.build();
  }


  private Form<Tuple, Tuple> fillout(final List<Factor> factors) {
    return new Form<Tuple, Tuple>() {
      int i = 0;

      @Override
      public Tuple apply(Tuple in) {
        Tuple projected = projectDontCaresOnly(in);
        for (Factor each : figureOutInvolvedFactors(factors, projected)) {
          if (each instanceof GroupedFactor) {
            projected.putAll(chooseLevelFromGroupedFactor(projected, (GroupedFactor) each));
          } else {
            projected.put(each.name, chooseLevelFromSimpleFactor(each));
          }
        }
        in.putAll(projected);
        return in;
      }

      private Object chooseLevelFromSimpleFactor(Factor factor) {
        return factor.levels.get(i++ % factor.levels.size());
      }

      private Tuple projectDontCaresOnly(Tuple in) {
        Tuple.Builder b = new Tuple.Builder();
        for (String key : in.keySet()) {
          if (in.get(key) == com.github.dakusui.jcunit8.core.Utils.DontCare) {
            b.put(key, com.github.dakusui.jcunit8.core.Utils.DontCare);
          }
        }
        return b.build();
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

  private Tuple chooseLevelFromGroupedFactor(Tuple σ, GroupedFactor groupedFactor) {
    final Tuple.Builder builder = new Tuple.Builder();
    for (String key : σ.keySet()) {
      if (groupedFactor.getSubfactorNames().contains(key)) {
        Object v = σ.get(key);
        if (!com.github.dakusui.jcunit8.core.Utils.DontCare.equals(v)) {
          builder.put(key, v);
        }
      }
    }
    final Tuple projected = builder.build();
    return filter(transform(groupedFactor,
        new Form<Object, Tuple>() {
          @Override
          public Tuple apply(Object in) {
            return (Tuple) in;
          }
        }),
        new Predicate<Tuple>() {
          @Override
          public boolean apply(Tuple in) {
            return TupleUtils.isSubtupleOf(projected, in);
          }
        }).get(0);
  }

  private List<Factor> figureOutInvolvedFactors(List<Factor> factors, Tuple σ) {
    List<Factor> ret = new ArrayList<Factor>(factors.size());
    for (String keyName : σ.keySet()) {
      for (Factor eachFactor : factors) {
        if (eachFactor instanceof GroupedFactor) {
          if (((GroupedFactor) eachFactor).getSubfactorNames().contains(keyName) && !ret.contains(eachFactor)) {
            ret.add(eachFactor);
          }
        } else {
          if (eachFactor.name.equals(keyName)) {
            ret.add(eachFactor);
          }
        }
      }
    }
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


  private Form<Tuple, Tuple> expandGroupedFactors(List<Factor> processedFactors) {
    final List<GroupedFactor> groupedFactors = transform(filter(processedFactors, isGroupedFactor()), castTo(GroupedFactor.class));
    return new Form<Tuple, Tuple>() {
      @Override
      public Tuple apply(Tuple in) {
        for (GroupedFactor each : groupedFactors) {
          if (in.containsKey(each.name))
            in.putAll((Tuple) in.remove(each.name));
        }
        return in;
      }
    };
  }

  private <T> Form<Object, T> castTo(final Class<T> clazz) {
    return new Form<Object, T>() {
      @Override
      public T apply(Object in) {
        return clazz.cast(in);
      }
    };
  }

  private static Predicate<Tuple> isSatisfying(
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

  private static Predicate<Factor> isGroupedFactor() {
    return new Predicate<Factor>() {
      @Override
      public boolean apply(Factor in) {
        return in instanceof GroupedFactor;
      }
    };
  }


  private List<Factor> arrangeFactors(final Factors factors, final List<Constraint> constraints, final int strength) {
    final List<Factor> ret = new ArrayList<Factor>(factors.asFactorList());
    List<GroupedFactor> groupedFactors = transform(
        groupFactorNamesUsedByConstraints(constraints),
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
            }), filter(constraints, isRelated(subfactors)), strength);
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
    return Utils.toList(findLargestDisjointedGroups(Utils.toLinkedHashSet(work)));
  }

  private LinkedHashSet<List<String>> findLargestDisjointedGroups(LinkedHashSet<List<String>> in) {
    while (anyConnectedPairs(in)) {
      in = mergeConnected(in);
    }
    return in;
  }

  private boolean anyConnectedPairs(LinkedHashSet<List<String>> in) {
    if (in.size() < 2) {
      return false;
    }
    for (List<List<String>> each : new Combinator<List<String>>(Utils.toList(in), 2)) {
      if (Utils.containsAny(each.get(0), each.get(1)))
        return true;
    }
    return false;
  }

  private LinkedHashSet<List<String>> mergeConnected(LinkedHashSet<List<String>> in) {
    LinkedHashSet<List<String>> ret = new LinkedHashSet<List<String>>();
    Combinator<List<String>> combinator = new Combinator<List<String>>(Utils.toList(in), 2);
    for (List<List<String>> each : combinator) {
      checkcond(each.size() == 2);
      List<String> a = each.get(0);
      List<String> b = each.get(1);
      if (Utils.containsAny(a, b)) {
        ret.remove(a);
        ret.remove(b);
        ret.add(merge(a, b));
      } else {
        ret.add(a);
        ret.add(b);
      }
    }
    return ret;
  }

  private <T> List<T> merge(List<T> a, List<T> b) {
    List<T> ret = new LinkedList<T>(a);
    _merge(ret, b);
    return ret;
  }


  private List<List<String>> _groupFactorNamesUsedByConstraints
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
      _merge(overlapping, outer);
    }
    return ret;
  }

  private static <T> void _merge(List<T> a, List<T> b) {
    for (T each : b) {
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
