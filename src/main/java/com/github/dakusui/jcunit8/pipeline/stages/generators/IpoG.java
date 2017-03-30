package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.core.StreamableCombinator;
import com.github.dakusui.jcunit8.core.StreamableTupleCartesianator;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo.DontCare;
import static java.util.stream.Collectors.toList;

abstract public class IpoG extends Generator.Base {
  final TupleSet precovered;

  IpoG(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
    super(seeds, factorSpace, requirement);
    this.precovered = new TupleSet.Builder().addAll(seeds.stream()
        .flatMap(tuple -> TupleUtils.subtuplesOf(tuple, requirement.strength()).stream())
        .collect(toList())).build();
  }

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
  @Override
  abstract public List<Tuple> generateCore();

  abstract TupleSet prepare_π(List<Factor> processedFactors, Factor factor, int strength);

  /*
   *  8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
   *             ..., vi-1 , vi ) so that τ’ covers the most number of
   *             combinations of values in π
   */
  abstract Optional<Object> chooseLevelThatCoversMostTuples(Tuple τ, Factor fi, TupleSet π, int t, List<Factor> allFactors, List<Constraint> fullyInvolvedConstraints, List<Constraint> partiallyInvolvedConstraints);

  Tuple modifyTupleWith(Tuple τ, String factorName, Object o1) {
    return new Tuple.Builder().putAll(τ).put(factorName, o1).build();
  }

  long countCoveredTuplesBy(Tuple τ$, final TupleSet π, int t) {
    return TupleUtils.subtuplesOf(τ$, t).stream()
        .filter(π::contains)
        .count();
  }

  Stream<Tuple> allPossibleTuples(List<Factor> factors, int strength) throws FrameworkException {
    //noinspection RedundantTypeArguments
    return new StreamableCombinator<>(
        factors.stream()
            .map(Factor::getName)
            .collect(toList()), strength)
        .stream()
        .flatMap((List<String> strings) -> new StreamableTupleCartesianator(
            strings.stream()
                .map(
                    (String s) -> factors.stream()
                        .filter(factor -> s.equals(factor.getName()))
                        .findFirst()
                        /*
                         * This explicit type parameter is necessary to suppress a compilation error in some
                         * JDK versions.
                         */
                        .<FrameworkException>orElseThrow(FrameworkException::unexpectedByDesign)
                )
                .collect(toList())).stream()
        );
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
  Stream<Tuple> chooseTestToCoverGivenTuple(final List<Factor> factors, List<Tuple> ts, final Tuple σ) {
    Predicate<Tuple> matches = current -> {
      for (Factor each : figureOutInvolvedFactors(factors, σ)) {
        Object currentLevel = current.get(each.getName());
        if (!(DontCare.equals(currentLevel) || Objects.equals(currentLevel, σ.get(each.getName())))) {
          return false;
        }
      }
      return true;
    };
    return ts.stream().filter(matches);
  }

  private List<Factor> figureOutInvolvedFactors(List<Factor> factors, Tuple σ) {
    return factors.stream()
        .filter(factor -> σ.keySet().contains(factor.getName()))
        .collect(toList());
  }

  /**
   * <pre>
   * 16. change an existing test, if possible, or otherwise add a new test
   *     to cover σ
   * </pre>
   */
  Tuple createTupleFrom(List<String> processedFactorNames, Tuple σ) {
    Tuple.Builder builder = new Tuple.Builder();
    for (String each : processedFactorNames) {
      builder.put(each, DontCare);
    }
    builder.putAll(σ);
    return builder.build();
  }

  Function<Tuple, Tuple> replaceDontCareValuesWithActualLevels(final List<Factor> factorsToBeExplored, List<Constraint> allInvolvedConstraints) {
    return new Function<Tuple, Tuple>() {
      int i = 0;

      @Override
      public Tuple apply(Tuple in) {
        Tuple projected = projectDontCaresOnly(in);
        for (Factor each : figureOutInvolvedFactors(factorsToBeExplored, projected)) {
          projected.put(each.getName(), chooseLevelFromSimpleFactor(each));
        }
        in.putAll(projected);
        return in;
      }

      private Object chooseLevelFromSimpleFactor(Factor factor) {
        return factor.getLevels().get(i++ % factor.getLevels().size());
      }

      private Tuple projectDontCaresOnly(Tuple in) {
        Tuple.Builder b = new Tuple.Builder();
        for (String key : in.keySet()) {
          if (in.get(key) == DontCare) {
            b.put(key, DontCare);
          }
        }
        return b.build();
      }
    };
  }

  /**
   * <pre>
   * 16. change an existing test, if possible, or otherwise add a new test
   *     to cover σ
   * </pre>
   */
  void modifyTestToCover(List<Factor> factors, Tuple chosenTest, Tuple σ) {
    // simple 'chosenTest.putAll(σ)' doesn't work because σ can contain values
    // under GroupedFactor, whose values picked up at once rather than one by one.
    for (Factor each : figureOutInvolvedFactors(factors, σ)) {
      chosenTest.put(each.getName(), σ.get(each.getName()));
    }
  }
}
