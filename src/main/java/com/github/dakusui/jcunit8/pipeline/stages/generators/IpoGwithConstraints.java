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
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.plugins.caengines.ipo2.Ipo.DontCare;
import static java.util.Collections.disjoint;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

public class IpoGwithConstraints extends IpoG {

  public IpoGwithConstraints(List<Tuple> seeds, Requirement requirement, FactorSpace factorSpace) {
    super(seeds, factorSpace, requirement);
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
  public List<Tuple> generateCore() {
    if (this.factorSpace.getFactors().size() == this.requirement.strength()) {
      return allPossibleTuples(this.factorSpace.getFactors(), this.requirement.strength())
          .filter(satisfiesAllOf(this.factorSpace.getConstraints())) // OVERRIDING
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
        .sorted(comparingInt(o -> -o.getLevels().size()))
        .collect(toList());
    List<Constraint> allConstraints = this.factorSpace.getConstraints();
    List<Tuple> ts = allPossibleTuples(this.factorSpace.getFactors().subList(0, t), t)
        .collect(toList());
    List<Factor> processedFactors = new LinkedList<>(allFactors.subList(0, t));
    int n = allFactors.size();
  /*
   *     4.  for (int i = t + 1 ; i ≤ n ; i ++ ){
     *         * t; strength
     *         * 0-origin
     */
    TupleSet π;
    for (int i = t + 1; i <= n; i++) {
    /*     5.    let π be the set of t -way combinations of values involving parameter
       *            Pi and t -1 parameters among the first i – 1 parameters
       */
      Factor Pi = allFactors.get(i - 1);
      π = prepare_π(processedFactors, Pi, t);
      ////
      // OVERRIDING
      List<String> processedFactorNames = processedFactors.stream().map(Factor::getName).collect(toList());
      List<Constraint> fullyInvolvedConstraints = getFullyInvolvedConstraints(
          processedFactorNames,
          allConstraints
      );
      List<Constraint> partiallyInvolvedConstraints = getPartiallyInvolvedConstraints(
          processedFactorNames,
          allConstraints
      );
      Set<String> allInvolvedFactorNamesNotInTuple = partiallyInvolvedConstraints.stream()
          .flatMap(constraint -> constraint.involvedKeys().stream())
          .filter(s -> !processedFactorNames.contains(s))
          .collect(toSet());
      List<Factor> allInvolvedFactorsNotInTuple = allFactors.stream()
          .filter(factor -> allInvolvedFactorNamesNotInTuple.contains(factor.getName()))
          .collect(toList());
      // OVERRIDING
      ////

      /*     6.     // horizontal extension for parameter Pi
       *     7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
       */
      for (Tuple τ : ts) {
        /*     8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
         *                ..., vi-1 , vi ) so that τ’ covers the most number of
         *                combinations of values in π
         */
        // OVERRIDING
        Object vi = chooseLevelThatCoversMostTuples(
            τ, Pi, π, t,
            allInvolvedFactorsNotInTuple,
            fullyInvolvedConstraints,
            partiallyInvolvedConstraints
        ).orElseThrow(FrameworkException::unexpectedByDesign);
        τ.put(Pi.getName(), vi);
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
              // OVERRIDING
              .filter(tuple -> satisfiesAllOf(getFullyInvolvedConstraints(
                  tuple.keySet().stream()
                      .filter(s -> !Objects.equals(tuple.get(s), DontCare))
                      .collect(toSet()),
                  allConstraints)).test(tuple))
              // OVERRIDING
              .filter(
                  tuple -> assignmentsAllowedByAllPartiallyInvolvedConstraints(
                      tuple,
                      involvedFactorsNotInTuple(allFactors, allConstraints, tuple.keySet()),
                      getPartiallyInvolvedConstraints(tuple.keySet(), allConstraints)
                  ).findFirst().isPresent())
              .findFirst()
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
      ts = ts.stream()
          .map(
              replaceDontCareValuesWithActualLevels(
                  Stream.concat(
                      processedFactors.stream(),
                      allInvolvedFactorsNotInTuple.stream()).collect(toList()),
                  Stream.concat(
                      fullyInvolvedConstraints.stream(),
                      partiallyInvolvedConstraints.stream()).collect(toList()))
          ).collect(toList());
    }
    ts.addAll(0, seeds);
    return ts;
  }

  private static List<Factor> involvedFactorsNotInTuple(List<Factor> factors, List<Constraint> allConstraints, Set<String> factorNamesInTuple) {
    return factors.stream()
        .filter(factor -> !factorNamesInTuple.contains(factor.getName()))
        .filter(((Predicate<Factor>) factor -> getFullyInvolvedConstraints(factorNamesInTuple, allConstraints).stream().flatMap(constraint -> constraint.involvedKeys().stream()).collect(toSet()).contains(factor.getName()))
            .or(factor -> getPartiallyInvolvedConstraints(factorNamesInTuple, allConstraints).stream().flatMap(constraint -> constraint.involvedKeys().stream()).collect(toSet()).contains(factor.getName())))
        .collect(toList());
  }

  @Override
  TupleSet prepare_π(List<Factor> processedFactors, Factor factor, int strength) {
    /*     5.     let π be the set of t -way combinations of values involving parameter
     *            Pi and t -1 parameters among the first i – 1 parameters
     */
    processedFactors.add(factor);
    return new TupleSet.Builder().addAll(
        new StreamableCombinator<>(
            processedFactors,
            strength
        ).stream()
            .flatMap((List<Factor> factors) -> new StreamableTupleCartesianator(factors).stream())
            .filter((Tuple tuple) -> !precovered.contains(tuple))
            .filter((Tuple tuple) -> satisfiesAllOf(constraintsFullyCoveredBy(this.factorSpace.getConstraints(), tuple)).test(tuple))
            .filter((Tuple tuple) -> assignmentsAllowedByAllPartiallyInvolvedConstraints(
                tuple,
                processedFactors.stream().filter((Factor eachFactor) -> !tuple.keySet().contains(eachFactor.getName())).collect(toList()),
                constraintsPartiallyCoveredBy(this.factorSpace.getConstraints(), tuple)
            ).findFirst().isPresent())
            .collect(toList()))
        .build();
  }

  private List<Constraint> constraintsFullyCoveredBy(List<Constraint> constraints, Tuple tuple) {
    return constraints.stream()
        .filter(constraint -> tuple.keySet().containsAll(constraints))
        .collect(toList());
  }

  private List<Constraint> constraintsPartiallyCoveredBy(List<Constraint> constraints, Tuple tuple) {
    return constraints.stream()
        .filter(constraint -> !tuple.keySet().containsAll(constraint.involvedKeys()))
        .filter(constraint -> !Collections.disjoint(tuple.keySet(), constraint.involvedKeys()))
        .collect(toList());
  }


  /*
   *  8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
   *             ..., vi-1 , vi ) so that τ’ covers the most number of
   *             combinations of values in π
   */
  @Override
  Optional<Object> chooseLevelThatCoversMostTuples(Tuple τ, Factor fi, TupleSet π, int t, List<Factor> allInvolvedFactorsNotInTuple, List<Constraint> fullyInvolvedConstraints, List<Constraint> partiallyInvolvedConstraints) {
    return fi.getLevels().stream()
        .map(o -> modifyTupleWith(τ, fi.getName(), o))
        .filter(
            tuple -> satisfiesAllOf(fullyInvolvedConstraints).test(tuple)
        )
        .filter(
            tuple -> assignmentsAllowedByAllPartiallyInvolvedConstraints(
                tuple,
                allInvolvedFactorsNotInTuple,
                partiallyInvolvedConstraints
            ).findFirst().isPresent()
        )
        .max(
            (Tuple t1, Tuple t2) ->
                (int) (countCoveredTuplesBy(t1, π, t) - countCoveredTuplesBy(t2, π, t))
        )
        .map((Tuple tuple) -> tuple.get(fi.getName()));
  }

  @Override
  Function<Tuple, Tuple> replaceDontCareValuesWithActualLevels(final List<Factor> factorsToBeExplored, List<Constraint> allInvolvedConstraints) {
    return new Function<Tuple, Tuple>() {
      int i = 0;
      int maxReadAheadSize = factorsToBeExplored.stream()
          .map(factor -> factor.getLevels().size())
          .max(Comparator.comparingInt(o -> o))
          .orElseThrow(FrameworkException::unexpectedByDesign);

      @Override
      public Tuple apply(Tuple in) {
        List<Factor> dontCareFactors = dontCareFactors(in, factorsToBeExplored);
        if (dontCareFactors.isEmpty())
          return in;
        i = (i + 1) % maxReadAheadSize;
        return new Tuple.Builder()
            .putAll(in)
            .putAll(
                chooseAssignment(
                    assignmentsForDontCaresUnderConstraints(
                        removeDontCares(in),
                        dontCareFactors),
                    i
                ).orElseThrow(FrameworkException::unexpectedByDesign)
            ).build();
      }

      Tuple removeDontCares(Tuple in) {
        Tuple.Builder builder = new Tuple.Builder();
        in.keySet().stream()
            .filter(s -> !DontCare.equals(in.get(s)))
            .forEach(s -> builder.put(s, in.get(s)));
        return builder.build();
      }

      private Optional<Tuple> chooseAssignment(Stream<Tuple> tupleStream, int index) {
        return get(read(Math.min(maxReadAheadSize, index), tupleStream), index);
      }

      private List<Tuple> read(int count, Stream<Tuple> tupleStream) {
        List<Tuple> back = tupleStream.collect(toList());
        return new AbstractList<Tuple>() {
          @Override
          public int size() {
            return count;
          }

          @Override
          public Tuple get(int index) {
            return index < back.size() ?
                back.get(index) :
                back.get(index % count);
          }
        };
      }

      private Optional<Tuple> get(List<Tuple> tuples, int i) {
        int size = tuples.size();
        if (size == 0)
          return Optional.empty();
        return Optional.of(tuples.get(i % size));
      }

      private Stream<Tuple> assignmentsForDontCaresUnderConstraints(Tuple in, List<Factor> factorsToBeExplored) {
        return new StreamableTupleCartesianator(factorsToBeExplored)
            .stream()
            .map(tuple -> new Tuple.Builder().putAll(in).build())
            .filter(
                tuple -> satisfiesAllOf(
                    getFullyInvolvedConstraints(
                        factorsToBeExplored.stream().map(Factor::getName).collect(toList()),
                        allInvolvedConstraints
                    )).test(tuple)
            )
            .filter(
                tuple -> assignmentsAllowedByAllPartiallyInvolvedConstraints(
                    tuple,
                    factorsToBeExplored,
                    getPartiallyInvolvedConstraints(
                        factorsToBeExplored.stream().map(Factor::getName).collect(toList()),
                        allInvolvedConstraints
                    )).findFirst().isPresent()
            );
      }

      List<Factor> dontCareFactors(Tuple tuple, List<Factor> factors) {
        return factors.stream()
            .filter(
                (Factor eachFactor) ->
                    tuple.containsKey(eachFactor.getName()) && tuple.get(eachFactor.getName()) == DontCare
            )
            .collect(toList());
      }
    };
  }

  private static Predicate<Tuple> satisfiesAllOf(List<Constraint> predicates) {
    return predicates.stream()
        .map((Function<Constraint, Predicate<Tuple>>) constraint -> constraint)
        .reduce(Predicate::and)
        .orElse(tuple -> true);
  }

  private static Stream<Tuple> assignmentsAllowedByAllPartiallyInvolvedConstraints(
      Tuple tuple,
      List<Factor> allFactors,
      List<Constraint> allConstraints
  ) {
    List<Constraint> partiallyInvolvedConstraints = getPartiallyInvolvedConstraints(
        allFactors.stream().map(Factor::getName).collect(toList()),
        allConstraints
    );

    return assignmentsAllowedByAllPartiallyInvolvedConstraintsPrivate(
        tuple,
        partiallyInvolvedConstraints.stream().flatMap(constraint -> constraint.involvedKeys().stream())
            .collect(toSet())
            .stream()
            .filter(s -> !tuple.containsKey(s))
            .map(s -> allFactors.stream().filter(factor -> factor.getName().equals(s)).findFirst().orElseThrow(FrameworkException::unexpectedByDesign))
            .collect(toList()),
        partiallyInvolvedConstraints
    );
  }

  private static Stream<Tuple> assignmentsAllowedByAllPartiallyInvolvedConstraintsPrivate(
      Tuple tuple,
      List<Factor> allInvolvedFactorsNotInTuple,
      List<Constraint> partiallyInvolvedConstraints
  ) {
    return new StreamableTupleCartesianator(allInvolvedFactorsNotInTuple).stream()
        .map(eachTuple -> new Tuple.Builder().putAll(tuple).putAll(eachTuple).build())
        .filter(
            (Tuple eachTuple) -> partiallyInvolvedConstraints.stream()
                .allMatch(
                    (Constraint constraint) -> constraint.test(eachTuple))
        );
  }

  private static List<Constraint> getPartiallyInvolvedConstraints(Collection<String> factorNames, List<Constraint> allConstraints) {
    return allConstraints.stream()
        .filter((Constraint eachConstraint) -> !factorNames.containsAll(eachConstraint.involvedKeys()))
        .filter((Constraint eachConstraint) -> !disjoint(eachConstraint.involvedKeys(), factorNames))
        .collect(toList());

  }

  private static List<Constraint> getFullyInvolvedConstraints(Collection<String> factorNames, List<Constraint> allConstraints) {
    return allConstraints.stream()
        .filter((Constraint eachConstraint) -> factorNames.containsAll(eachConstraint.involvedKeys()))
        .collect(toList());
  }
}
