package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.core.StreamableCombinator;
import com.github.dakusui.jcunit8.core.StreamableTupleCartesianator;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo.DontCare;
import static java.util.Arrays.asList;
import static java.util.Collections.disjoint;
import static java.util.stream.Collectors.toList;

public class IpoG extends Generator.Base {
  public IpoG(List<Tuple> seeds, FactorSpace factorSpace, Requirement requirement) {
    super(seeds, factorSpace, requirement);
  }

  private static Predicate<Tuple> satisfiesAllOf(List<Constraint> predicates) {
    return predicates.stream()
        .map((Function<Constraint, Predicate<Tuple>>) constraint -> constraint)
        .reduce((tuplePredicate, tuplePredicate2) -> null)
        .orElse(tuple -> true);
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
  public List<Tuple> generate() {
    if (this.factorSpace.getFactors().size() == this.requirement.strength()) {
      return allPossibleTuples(this.factorSpace.getFactors(), this.requirement.strength())
          .filter(satisfiesAllOf(this.factorSpace.getConstraints()))
          .collect(toList());
    }

    /*
     *   Algorithm: IPOG-Test (int t , ParameterSet ps ) {
     *     1.  initialize test set ts to be an empty set
     *     2.  denote the parameters in ps , in an arbitrary order, as P1 , P2, ...,
     *         and Pn
     *     3.  add into test set ts a test for each combination of values of the first
     *         t parameters
     */
    int t = this.requirement.strength();
    List<Factor> allFactors = this.factorSpace.getFactors().stream()
        .sorted(Comparator.comparingInt(o -> -o.getLevels().size()))
        .collect(toList());
    List<Tuple> ts = allPossibleTuples(this.factorSpace.getFactors(), t)
        .collect(toList());
    List<Factor> processedFactors = new LinkedList<>(allFactors.subList(0, t));
    int n = allFactors.size();
    /*
     *     4.  for (int i = t + 1 ; i ≤ n ; i ++ ){
     *         * t; strength
     *         * 0-origin
     */
    List<Tuple> π;
    for (int i = t + 1; i <= n; i++) {
      Factor Pi = allFactors.get(i - 1);
      /*     5.    let π be the set of t -way combinations of values involving parameter
       *            Pi and t -1 parameters among the first i – 1 parameters
       */
      π = prepare_π(processedFactors, Pi, t).collect(toList());
      /*     6.     // horizontal extension for parameter Pi
       *     7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
       */
      for (Tuple τ : ts) {
        /*     8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
         *                ..., vi-1 , vi ) so that τ’ covers the most number of
         *                combinations of values in π
         */
        Object vi = chooseLevelThatCoversMostTuples(Pi, τ, π, t);
        /*  9.         remove from π the combinations of values covered by τ’
         */
        π.removeAll(TupleUtils.subtuplesOf(modifyTupleWith(τ, Pi.getName(), vi), t));
      }
      /* 10.
       * 11.    // vertical extension for parameter P i
       * 12.    for (each combination σ in set π ) {
       */
      for (Tuple σ : new LinkedList<>(π)) {
        /* 13.      if (there exists a test that already covers σ ) {
         * 14.          remove σ from π
         * 15.      } else {
         * 16.        change an existing test, if possible, or otherwise add a new test
         *            to cover σ and remove it from π
         * 17.      }
         */
        if (ts.stream().anyMatch((Tuple each) -> TupleUtils.isSubtupleOf(σ, each))) {
          π.remove(σ);
        } else {
          List<Tuple> work = ts;
          Tuple chosenTest = chooseTestToCoverGivenTuple(processedFactors, ts, σ)
              .orElseGet(() -> {
                Tuple ret = createTupleFrom(
                    processedFactors.stream().map(Factor::getName).collect(toList()),
                    σ
                );
                work.add(ret);
                return ret;
              });
          modifyTestToCover(processedFactors, chosenTest, σ);
          π.remove(σ);
        }
      }
      ts = Utils.unique(ts.stream().map(fillout(processedFactors)).collect(toList()));
    }
    return ts;
  }

  private Stream<Tuple> prepare_π(List<Factor> processedFactors, Factor factor, int strength) {
    /*     5.     let π be the set of t -way combinations of values involving parameter
     *            Pi and t -1 parameters among the first i – 1 parameters
     */
    processedFactors.add(factor);
    return new StreamableCombinator<>(
        Stream.concat(processedFactors.stream(), Stream.of(factor)).collect(toList()), strength)
        .stream()
        .flatMap(factors -> new StreamableTupleCartesianator(factors).stream());
  }

  /*
   *  8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
   *             ..., vi-1 , vi ) so that τ’ covers the most number of
   *             combinations of values in π
   */
  private Object chooseLevelThatCoversMostTuples(Factor fi, Tuple τ, List<Tuple> π, int t) {
    return fi.getLevels().stream()
        .max((o1, o2) -> {
          Tuple t1 = modifyTupleWith(τ, fi.getName(), o1);
          Tuple t2 = modifyTupleWith(τ, fi.getName(), o2);
          return (int) (countCoveredTuplesBy(t1, π, t) - countCoveredTuplesBy(t2, π, t));
        });
  }

  private Tuple modifyTupleWith(Tuple τ, String factorName, Object o1) {
    return new Tuple.Builder().putAll(τ).put(factorName, o1).build();
  }

  private long countCoveredTuplesBy(Tuple τ$, final List<Tuple> π, int t) {
    return TupleUtils.subtuplesOf(τ$, t).stream()
        .filter(π::contains)
        .count();
  }

  /*
 *  13.      if (there exists a test that already covers σ ) {
 */
  private boolean containsTestThatCovers(List<Tuple> ts, Tuple σ) {
    return ts.stream().anyMatch(each -> TupleUtils.isSubtupleOf(σ, each));
  }


  private Stream<Tuple> allPossibleTuples(List<Factor> factors, int strength) throws FrameworkException {
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
  private Optional<Tuple> chooseTestToCoverGivenTuple(final List<Factor> factors, List<Tuple> ts, final Tuple σ) {
    Predicate<Tuple> matches = new Predicate<Tuple>() {
      @Override
      public boolean test(Tuple current) {
        boolean ret = true;
        for (Factor each : figureOutInvolvedFactors(factors, σ)) {
          Object currentLevel = current.get(each.getName());
          if (!(DontCare.equals(currentLevel) || Objects.equals(currentLevel, σ.get(each.getName())))) {
            ret = false;
          }
        }
        return ret;
      }
    };
    return ts.stream().filter(matches).findFirst();
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
  private Tuple createTupleFrom(List<String> processedFactorNames, Tuple σ) {
    Tuple.Builder builder = new Tuple.Builder();
    for (String each : processedFactorNames) {
      builder.put(each, DontCare);
    }
    builder.putAll(σ);
    return builder.build();
  }

  private Function<Tuple, Tuple> fillout(final List<Factor> factors) {
    return new Function<Tuple, Tuple>() {
      int i = 0;

      @Override
      public Tuple apply(Tuple in) {
        Tuple projected = projectDontCaresOnly(in);
        for (Factor each : figureOutInvolvedFactors(factors, projected)) {
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
  private void modifyTestToCover(List<Factor> factors, Tuple chosenTest, Tuple σ) {
    // simple 'chosenTest.putAll(σ)' doesn't work because σ can contain values
    // under GroupedFactor, whose values picked up at once rather than one by one.
    for (Factor each : figureOutInvolvedFactors(factors, σ)) {
      chosenTest.put(each.getName(), σ.get(each.getName()));
    }
  }


  private boolean isAllowedByAllPartiallyInvolvedConstraints(Tuple tuple, List<Factor> allFactors, List<Constraint> allConstraints) {
    List<Constraint> partiallyInvolvedConstraints = allConstraints.stream()
        .filter((Constraint eachConstraint) -> !tuple.keySet().containsAll(eachConstraint.involvedKeys()))
        .filter((Constraint eachConstraint) -> !disjoint(eachConstraint.involvedKeys(), tuple.keySet()))
        .collect(toList());
    List<String> meaningfulFactorNames = Utils.unique(partiallyInvolvedConstraints.stream()
        .flatMap(constraint -> constraint.involvedKeys().stream())
        .filter(s -> !tuple.keySet().contains(s))
        .collect(toList()));
    return new StreamableTupleCartesianator(
        allFactors.stream()
            .filter(factor -> meaningfulFactorNames.contains(factor.getName()))
            .collect(toList())
    ).stream()
        .anyMatch(
            (Tuple eachTuple) -> partiallyInvolvedConstraints.stream()
                .allMatch(
                    (Constraint constraint) -> constraint.test(eachTuple)));
  }

  private List<Constraint> getFullyInvolvedConstraints(List<String> factorNames, List<Constraint> allConstraints) {
    return allConstraints.stream()
        .filter(constraint -> factorNames.containsAll(constraint.involvedKeys()))
        .collect(toList());
  }

  public static void main(String... args) {
    System.out.println(asList(1, 2, 3)
        .stream()
        .sorted((o1, o2) -> -(o1 - o2))
        .collect(toList()));

  }
}
