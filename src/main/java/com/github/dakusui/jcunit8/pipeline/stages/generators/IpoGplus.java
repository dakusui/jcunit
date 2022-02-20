package com.github.dakusui.jcunit8.pipeline.stages.generators;

import com.github.dakusui.jcunit.core.tuples.Aarray;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.core.StreamableCombinator;
import com.github.dakusui.jcunit8.core.StreamableTupleCartesianator;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.FactorUtils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Collections.disjoint;
import static java.util.Collections.emptyList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("NonAsciiCharacters")
public class IpoGplus extends Generator.Base {
  public static class Session {
    private final AtomicInteger optimizer                      = new AtomicInteger(0);
    /**
     * A curried function to find first tuple under constraints, which is memoized.
     */
    private final Function<List<Constraint>, Function<List<Factor>, Optional<Aarray>>>
                                findFirstTupleUnderConstraints = Utils.memoize(functionToFindFirstTupleUnderConstraints());

    private Map<String, Object> chooseAssignmentsFor(List<Factor> dontCareFactors) {
      return new HashMap<String, Object>() {{
        dontCareFactors.forEach(factor -> put(factor.getName(), factor.getLevels().get(optimizer.getAndIncrement() % factor.getLevels().size())));
      }};
    }
  }

  private final Session session;

  private final TupleSet precovered;

  public IpoGplus(FactorSpace factorSpace, Requirement requirement, List<Aarray> seeds) {
    super(factorSpace, requirement);
    this.session = new Session();
    this.precovered = new TupleSet.Builder().addAll(
        seeds.stream(
        ).filter(
            tuple -> tuple.keySet().containsAll(factorSpace.getFactorNames())
        ).filter(
            ////
            // tuples covered by negative tests should not be considered
            // covered.
            tuple -> factorSpace.getConstraints().stream()
                .allMatch(
                    constraint -> constraint.test(tuple)
                )
        ).map(
            tuple -> TupleUtils.project(tuple, factorSpace.getFactorNames())
        ).flatMap(
            tuple -> TupleUtils.subtuplesOf(tuple, requirement.strength()).stream()
        ).collect(
            toList()
        )
    ).build();
  }

  /**
   * <pre>
   *   Algorithm: IPOG-Test (int t , ParameterSet ps ) {
   *     1.  initialize test set ts to be an empty set
   *     2.  denote the parameters in ps , in an arbitrary order, as P1 , P2, ...,
   *         and Pn
   *     3.  add into test set ts a test for each combination of values of the first
   *         t parameters (*1)
   *     4.  for (int i = t + 1 ; i ≤ n ; i ++ ){
   *     5.     let π be the set of t-way combinations of values involving parameter
   *            Pi and t -1 parameters among the first i – 1 parameters (*2)
   *     6.     // horizontal extension for parameter Pi
   *     7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
   *     8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
   *                ..., vi-1 , vi ) so that τ’ covers the most number of
   *                combinations of values in π (*3)
   *     9.         remove from π the combinations of values covered by τ’
   *     10.    }
   *     11.    // vertical extension for parameter P i
   *     12.    for (each combination σ in set π ) {
   *     13.      if (there exists a test that already covers σ ) {
   *     14.          remove σ from π
   *     15.      } else {
   *     16.          change an existing test, if possible, or otherwise add a new test
   *                  to cover σ and remove it from π (*4) (*a)
   *     17.      }
   *     18.    }
   *     19.  }
   *     20.  return ts;
   *    }
   *   See http://barbie.uta.edu/~fduan/ACTS/IPOG_%20A%20General%20Strategy%20for%20T-Way%20Software%20Testing.pdf
   *
   *   Constraint handling consideration (if an impossible constraint is given)
   *   (*1)  If one or more impossible constraints are involved in first t parameters,
   *         ts can become empty. This method should return an empty set immediately.
   *   (*2)  If one or more impossible constraints are involved in first i-1 parameters,
   *         π will become empty.
   *   (*3)
   *   (*4)
   * </pre>
   */
  @Override
  public List<Aarray> generateCore() {
    if (this.factorSpace.getFactors().size() == this.requirement.strength()) {
      return streamAllPossibleTuples(this.factorSpace.getFactors(), this.requirement.strength())
          .filter(satisfiesAllOf(this.factorSpace.getConstraints())) // OVERRIDING
          .collect(toList());
    }

    /*
     *   Algorithm: IPOG-Test (int t , ParameterSet ps ) {
     *     1.  initialize test set ts to be an empty set
     *     2.  denote the parameters in ps , in an arbitrary order, as P1 , P2, ...,
     *         and Pn
     *     3.  add into test set ts a test for each combination of values of the first
     *         t parameters (*1)
     */
    int t = this.requirement.strength();
    List<Factor> allFactors = this.factorSpace.getFactors().stream()
        .sorted(comparingInt(o -> -o.getLevels().size()))
        .collect(toList());
    List<Constraint> allConstraints = this.factorSpace.getConstraints();
    List<Aarray> ts = streamAllPossibleTuples(allFactors.subList(0, t), t)
        .filter(isAllowedTuple(allFactors, allConstraints, session)) // (*1)
        .filter(tuple -> !this.precovered.contains(tuple))
        .collect(toList());
    if (ts.isEmpty())
      return emptyList();
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
       *            Pi and t -1 parameters among the first i – 1 parameters (*2)
       */
      Factor Pi = allFactors.get(i - 1);
      processedFactors.add(Pi);
      π = prepare_π(processedFactors, allFactors, allConstraints, t);
      /*     6.     // horizontal extension for parameter Pi
       *     7.     for (each test τ = (v 1 , v 2 , ..., v i-1 ) in test set ts ) {
       */
      for (Aarray τ : ts) {
        /*     8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
         *                ..., vi-1 , vi ) so that τ’ covers the most number of
         *                combinations of values in π (*3)
         */
        Object vi = chooseLevelThatCoversMostTuples(
            τ, Pi, π, t,
            allFactors,
            allConstraints,
            session
        ).orElseThrow(
            ////
            // (*3) This cannot happen
            () -> TestDefinitionException.failedToCover(Pi.getName(), Pi.getLevels(), τ)
        );
        τ.put(Pi.getName(), vi);
        /*  9.         remove from π the combinations of values covered by τ’
         */
        π.removeAll(TupleUtils.subtuplesOf(τ, t));
      }

      /* 10.
       * 11.    // vertical extension for parameter P i
       * 12.    for (each combination σ in set π ) {
       */
      for (Aarray σ : new LinkedList<>(π)) {
        /* 13.      if (there exists a test that already covers σ ) {
         * 14.          remove σ from π
         * 15.      } else {
         * 16.        change an existing test, if possible, or otherwise add a new test
         *            to cover σ and remove it from π (*4)
         * 17.      }
         */
        if (ts.stream().anyMatch(σ::isSubtupleOf)) {
          π.remove(σ);
        } else {
          Aarray chosenTest = streamIncompleteTestsToCoverGivenTuple(
              ts, σ
          ).filter(
              (Aarray tuple) -> isAllowedTuple(allFactors, allConstraints, this.session).test(
                  Aarray.builder().putAll(removeDontCares(tuple)).putAll(σ).build()
              ) // (*4)
          ).findFirst(
          ).orElseGet(
              () -> createTupleFrom(
                  FactorUtils.toFactorNames(processedFactors),
                  σ
              )
          );
          /*
           * <pre>
           * 16. change an existing test, if possible, or otherwise add a new test
           *     to cover σ (*a)
           * </pre>
           */
          chosenTest.putAll(σ);
          if (!ts.contains(chosenTest))
            ts.add(chosenTest);
          π.remove(σ);
        }
      }
      ts = ts.stream()
          .map(
              replaceDontCareValuesWithActualLevels(
                  allFactors,
                  allConstraints,
                  session
              )
          ).collect(toList());
    }
    return ts;
  }

  @SuppressWarnings("WeakerAccess")
  protected void validate() {
    FrameworkException.checkCondition(
        this.factorSpace.getFactors().size() >= requirement.strength(),
        FrameworkException::unexpectedByDesign,
        () -> String.format(
            "Required strength (%d) > Only %d factors are given: %s",
            this.requirement.strength(),
            this.factorSpace.getFactors().size(),
            this.factorSpace.getFactorNames()
        )
    );
  }

  private TupleSet prepare_π(List<Factor> alreadyProcessedFactors, List<Factor> allFactors, List<Constraint> allConstraints, int strength) {
    /*     5.     let π be the set of t -way combinations of values involving parameter
     *            Pi and t -1 parameters among the first i – 1 parameters (*2)
     *
     */
    return new TupleSet.Builder().addAll(
        new StreamableCombinator<>(
            alreadyProcessedFactors,
            strength
        ).stream()
            .flatMap((List<Factor> factors) -> new StreamableTupleCartesianator(factors).stream())
            .filter((Aarray tuple) -> !precovered.contains(tuple))
            .filter(isAllowedTuple(allFactors, allConstraints, session)) // (*2)
            .collect(toList()))
        .build();
  }

  /*
   *  8.         choose a value vi of Pi and replace τ with τ’ = (v 1 , v 2 ,
   *             ..., vi-1 , vi ) so that τ’ covers the most number of
   *             combinations of values in π (*3)
   */
  private static Optional<Object> chooseLevelThatCoversMostTuples(Aarray τ, Factor fi, TupleSet π, int t, List<Factor> allFactors, List<Constraint> allConstraints, Session session) {
    return fi.getLevels().stream()
        .map((Object eachLevel) -> modifyTupleWith(τ, fi.getName(), eachLevel))
        .filter(isAllowedTuple(allFactors, allConstraints, session)) // (*3)
        .max(
            (Aarray t1, Aarray t2) ->
                (int) (countCoveredTuplesBy(t1, π, t) - countCoveredTuplesBy(t2, π, t))
        )
        .map((Aarray tuple) -> tuple.get(fi.getName()));
  }

  private static Aarray modifyTupleWith(Aarray τ, String factorName, Object o1) {
    return new Aarray.Builder().putAll(τ).put(factorName, o1).build();
  }

  /**
   * Counts number of tuples in {@code π} covered by {@code τ$}.
   *
   * @param τ$ A tuple to cover tuples in π.
   * @param π  A set of tuples to be covered by {@code τ$}.
   * @param t  strength
   */
  private static long countCoveredTuplesBy(Aarray τ$, final TupleSet π, int t) {
    return TupleUtils.subtuplesOf(τ$, t).stream()
        .filter(π::contains)
        .count();
  }

  /**
   * <pre>
   * 16. change an existing test, if possible, or otherwise add a new test
   *     to cover σ
   * </pre>
   */
  private static Aarray createTupleFrom(List<String> factorNames, Aarray σ) {
    Aarray.Builder builder = new Aarray.Builder();
    for (String each : factorNames) {
      builder.put(each, DontCare);
    }
    builder.putAll(σ);
    return builder.build();
  }

  public static Function<Aarray, Aarray> replaceDontCareValuesWithActualLevels(final List<Factor> allFactors, List<Constraint> allConstraints, Session session) {
    return new Function<Aarray, Aarray>() {
      int i = 0;
      int maxReadAheadSize = allFactors.stream()
          .map(factor -> factor.getLevels().size())
          .max(comparingInt(o -> o))
          .orElseThrow(FrameworkException::unexpectedByDesign);

      @Override
      public Aarray apply(Aarray in) {
        List<Factor> dontCareFactors = dontCareFactors(in, allFactors);
        if (dontCareFactors.isEmpty())
          return in;
        i = i % maxReadAheadSize;
        return new Aarray.Builder()
            .putAll(in)
            .putAll(
                chooseAssignment(
                    streamAssignmentsForDontCaresUnderConstraints(
                        in,
                        allFactors,
                        allConstraints,
                        session
                    ), // (*a)
                    i++
                ).orElseThrow(() -> TestDefinitionException.impossibleConstraint(allConstraints))
            ).build();
      }

      private Optional<Aarray> chooseAssignment(Stream<Aarray> tupleStream, int index) {
        List<Aarray> work = tupleStream.limit(index + 1).collect(toList());
        return work.isEmpty() ?
            Optional.empty() :
            Optional.of(work.get(index % work.size()));
      }

    };
  }

  private static List<Factor> dontCareFactors(Aarray tuple, List<Factor> factors) {
    return factors.stream()
        .filter(
            (Factor eachFactor) ->
                tuple.containsKey(eachFactor.getName()) && tuple.get(eachFactor.getName()) == DontCare
        )
        .collect(toList());
  }

  private static Aarray removeDontCares(Aarray in) {
    Aarray.Builder builder = new Aarray.Builder();
    in.keySet().stream()
        .filter(s -> !DontCare.equals(in.get(s)))
        .forEach(s -> builder.put(s, in.get(s)));
    return builder.build();
  }

  public static Stream<Aarray> streamAllPossibleTuples(List<Factor> factors, int strength) throws FrameworkException {
    FrameworkException.checkCondition(
        factors.size() >= strength
    );
    Map<String, Factor> factorValues = new HashMap<String, Factor>() {{
      factors.forEach(factor -> put(factor.getName(), factor));
    }};
    //noinspection RedundantTypeArguments
    return new StreamableCombinator<>(
        FactorUtils.toFactorNames(factors),
        strength
    ).stream()
        .flatMap((List<String> chosenFactorNames) -> new StreamableTupleCartesianator(
                chosenFactorNames.stream()
                    .map(factorValues::get)
                    .collect(toList())
            ).stream()
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
   * This method chooses tests from ts by
   *
   * @param ts A set of (incomplete) tests.
   * @param σ  A tuple to be covered.
   * @return A stream of possible incomplete tests that cover σ.
   */
  @SuppressWarnings("NonAsciiCharacters")
  public static Stream<Aarray> streamIncompleteTestsToCoverGivenTuple(List<Aarray> ts, final Aarray σ) {
    return ts.stream()
        .filter((Aarray each) -> σ.keySet().stream()
            .allMatch(eachFactorNameIn_σ -> {
              if (!each.containsKey(eachFactorNameIn_σ))
                return true;
              Object eachLevel = each.get(eachFactorNameIn_σ);
              return Objects.equals(eachLevel, DontCare) || Objects.equals(eachLevel, σ.get(eachFactorNameIn_σ));
            }));
  }

  public static Stream<Aarray> streamAssignmentsForDontCaresUnderConstraints(
      Aarray in,
      List<Factor> allFactors,
      List<Constraint> allConstraints,
      Session session
  ) {
    List<Factor> dontCareFactors = dontCareFactors(in, allFactors);
    if (allConstraints.isEmpty())
      return Stream.of(new Aarray.Builder().putAll(removeDontCares(in)).putAll(session.chooseAssignmentsFor(dontCareFactors)).build());
    return new StreamableTupleCartesianator(dontCareFactors).stream()
        .flatMap(tuple -> streamAssignmentsAllowedByConstraints(
            new Aarray.Builder().putAll(removeDontCares(in)).putAll(tuple).build(),
            allFactors,
            allConstraints,
            session
        ));
  }

  public static Stream<Aarray> streamAssignmentsAllowedByConstraints(
      Aarray request,
      List<Factor> allFactors,
      List<Constraint> allConstraints,
      Session session
  ) {
    List<Factor> factorsUnderConstraintsInRequest = factorsUnderConstrains(allFactors, allConstraints).stream(
    ).map(
        factor -> (!request.containsKey(factor.getName()) || request.get(factor.getName()) == DontCare) ?
            factor :
            Factor.create(factor.getName(), new Object[] { request.get(factor.getName()) })
    ).collect(toList());

    return _streamAssignmentsAllowedByConstraints(request, allConstraints, factorsUnderConstraintsInRequest, session);
  }

  public static Function<List<Factor>, Stream<Aarray>> streamTuplesUnderConstraints(List<Constraint> allConstraints) {
    return factorsUnderConstraintsInRequest -> new StreamableTupleCartesianator(
        factorsUnderConstraintsInRequest
    ).stream(
    ).filter(
        satisfies(allConstraints)
    );
  }

  public static Predicate<Aarray> satisfiesAllOf(List<Constraint> predicates) {
    return predicates.stream()
        .map((Function<Constraint, Predicate<Aarray>>) constraint -> constraint)
        .reduce(Predicate::and)
        .orElse(tuple -> true);
  }

  public static List<Constraint> getFullyInvolvedConstraints(Collection<String> assignedFactorNames, List<Constraint> allConstraints) {
    return allConstraints.stream()
        .filter((Constraint eachConstraint) -> assignedFactorNames.containsAll(eachConstraint.involvedKeys()))
        .collect(toList());
  }

  public static List<Constraint> getPartiallyInvolvedConstraints(Collection<String> assignedFactorNames, List<Constraint> allConstraints) {
    return allConstraints.stream()
        .filter((Constraint eachConstraint) -> !assignedFactorNames.containsAll(eachConstraint.involvedKeys()))
        .filter((Constraint eachConstraint) -> !disjoint(eachConstraint.involvedKeys(), assignedFactorNames))
        .collect(toList());

  }

  private static Stream<Aarray> _streamAssignmentsAllowedByConstraints(
      Aarray request,
      List<Constraint> allConstraints,
      List<Factor> factorsUnderConstraintsInRequest,
      Session session
  ) {
    Optional<Aarray> firstTuple = session.findFirstTupleUnderConstraints.apply(allConstraints).apply(factorsUnderConstraintsInRequest);
    if (firstTuple.isPresent()) {
      StreamableTupleCartesianator cartesianator = new StreamableTupleCartesianator(
          factorsUnderConstraintsInRequest
      );
      return cartesianator
          .cursor(firstTuple.get())
          .stream(
          ).filter(
              satisfiesAllOf(allConstraints)
          ).map(
              tuple -> Aarray.builder().putAll(request).putAll(tuple).build()
          );
    }
    return Stream.empty();
  }

  private static Function<List<Constraint>, Function<List<Factor>, Optional<Aarray>>> functionToFindFirstTupleUnderConstraints() {
    return Utils.memoize(IpoGplus::findFirstTupleUnderConstraints);
  }

  private static Function<List<Factor>, Optional<Aarray>> findFirstTupleUnderConstraints(List<Constraint> allConstraints) {
    return (List<Factor> factorsUnderConstrains) ->
        streamTuplesUnderConstraints(allConstraints).apply(factorsUnderConstrains).findFirst();
  }

  private static Predicate<Aarray> satisfies(List<Constraint> allConstraints) {
    return tuple -> allConstraints.stream().allMatch(constraint -> constraint.test(tuple));
  }

  private static List<Factor> factorsUnderConstrains(List<Factor> allFactors, List<Constraint> allConstraints) {
    return allFactors.stream(
    ).filter(
        factor -> allConstraints.stream().anyMatch(constraint -> constraint.involvedKeys().contains(factor.getName()))
    ).collect(
        toList()
    );
  }

  private static Predicate<Aarray> isAllowedTuple(List<Factor> allFactors, List<Constraint> allConstraints, Session session) {
    return (Aarray tuple) -> streamAssignmentsAllowedByConstraints(
        tuple,
        allFactors,
        allConstraints,
        session
    ).findFirst().isPresent();
  }
}
