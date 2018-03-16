package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.core.StreamableCombinator;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.project;
import static com.github.dakusui.jcunit8.core.Utils.*;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("NonAsciiCharacters")
public class Florence extends Joiner.Base {
  final Requirement requirement;

  public Florence(Requirement requirement) {
    this.requirement = requirement;
  }

  protected long sizeOf(SchemafulTupleSet tupleSet) {
    return tupleSet.width();
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
   * // https://www.ncbi.nlm.nih.gov/pmc/articles/PMC4652878/
   *  </pre>
   */
  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    int t = this.requirement.strength();
    Session session = new Session();

    List<String> alreadyProcessedFactors = new LinkedList<>();
    Set<Tuple> used = new LinkedHashSet<>();
    TupleSet.Builder ts = new TupleSet.Builder().addAll(lhs);
    for (int i = 0; i < rhs.width(); i++) {
      long beforePreparation = System.currentTimeMillis();
      String F = rhs.getAttributeNames().get(i);
      TupleSet π = session.allPossibleUniqueTuplesOfStrength(
          lhs,
          rhs,
          alreadyProcessedFactors,
          F,
          t
      );
      final List<String> involvedFactors = concat(alreadyProcessedFactors.stream(), Stream.of(F)).collect(toList());
      final List<List<String>> tWayFactorNameSets = session
          .streamFactorNameSets(lhs.getAttributeNames(), alreadyProcessedFactors, F, t)
          .collect(toList());
      debug("preparation:π.size=%s,ts.size=%s:%s[msec]", π.size(), ts.content().size(), System.currentTimeMillis() - beforePreparation);
      ////
      // hg
      long beforeHg = System.currentTimeMillis();
      int sizeOfπBeforeHg = π.size();
      try {

        int tuplesRemovedLastTime = -1;
        Session.Hg hg = new Session.Hg();
        for (Tuple τ : new ArrayList<>(ts.content())) {
          int sizeOfπBeforeRemoval = π.size();
          hg.max(Math.min(
              tuplesRemovedLastTime == -1 ?
                  Long.MAX_VALUE :
                  tuplesRemovedLastTime,
              sizeOfπBeforeHg
          ));
          boolean τIsFullTuple = τ.containsKey(F);
          Object vi = τIsFullTuple ?
              τ.get(F) :
              session.chooseLevelThatCoversMostTuplesIn(τ, F, π, rhs, involvedFactors, tWayFactorNameSets, hg);
          Tuple.Builder b = Tuple.builder().putAll(τ);
          List<Tuple> candidates = rhs.index().find(
              project(
                  b.put(F, vi).build(),
                  involvedFactors
              )
          );
          assert !candidates.isEmpty();
          chooseCandidateIfOnlyOne(b, used, candidates);
          Tuple newTuple = b.build();
          π.removeAll(
              tuplesNewlyCovered(lhs.getAttributeNames(), alreadyProcessedFactors, F, vi, t, τ)
          );
          tuplesRemovedLastTime = τIsFullTuple ?
              tuplesRemovedLastTime :
              sizeOfπBeforeRemoval - π.size();
          ts.remove(τ);
          ts.add(newTuple);
        }
      } finally {
        debug("hg:π.size=%s,ts.size=%s:%s[msec]",
            π.size(), ts.content().size(), (System.currentTimeMillis() - beforeHg));
      }
      ////
      // vg
      long beforeVg = System.currentTimeMillis();
      try {
        int ii = 0;
        Session.Vg vg = new Session.Vg();
        while (!π.isEmpty()) {
          long beforeVg_i = System.currentTimeMillis();
          try {
            int sizeOfπBeforeRemoval = π.size();
            Tuple n = session.chooseBestCombination(
                π,
                lhs,
                rhs,
                involvedFactors,
                vg
            );
            π.removeAll(
                tuplesNewlyCovered(lhs.getAttributeNames(), alreadyProcessedFactors, F, n.get(F), t, n)
            );
            ts.add(n);
            vg.updateMaxFor(
                project(n, lhs.getAttributeNames()), project(n, rhs.getAttributeNames()), sizeOfπBeforeRemoval - π.size());
          } finally {
            debug("vg[%s]:%s:%s:%s", ii, π.size(), ts.content().size(), (System.currentTimeMillis() - beforeVg_i));
            ii++;
          }
        }
      } finally {
        debug("vg:" + π.size() + ":" + ts.content().size() + ":" + (System.currentTimeMillis() - beforeVg));
      }
      alreadyProcessedFactors = involvedFactors;
    }
    return new SchemafulTupleSet.Builder(
        new ArrayList<String>() {{
          addAll(lhs.getAttributeNames());
          addAll(rhs.getAttributeNames());
        }}
    ).addAllEntries(
        session.ensureAllTuplesAreUsed(
            ts,
            rhs
        ).content().stream().distinct().collect(toList())
    ).build();
  }

  private void chooseCandidateIfOnlyOne(Tuple.Builder b, Set<Tuple> used, List<Tuple> candidates) {
    if (candidates.size() == 1) {
      b.putAll(candidates.get(0));
      used.add(candidates.get(0));
      return;
    }
    List<Tuple> notUsedCandidates = (candidates.stream().filter(tuple -> !used.contains(tuple)).collect(toList()));
    if (notUsedCandidates.size() == 1) {
      b.putAll(notUsedCandidates.get(0));
      used.add(notUsedCandidates.get(0));
    }
  }

  private List<Tuple> tuplesNewlyCovered(List<String> factorsFromLhs, List<String> factorsFromRhs, String currentFactor, Object valueForCurrentFactor, int t, Tuple τ) {
    return combinations(
        append(factorsFromLhs, factorsFromRhs), t - 1
    ).map(factorNames -> new Tuple.Builder() {
      {
        factorNames.forEach(k -> put(k, τ.get(k)));
      }
    }.put(currentFactor, valueForCurrentFactor).build()).collect(toList());
  }

  static class Session {
    private final Function<SchemafulTupleSet, Function<List<String>, TupleSet>>          uniqueTuplesFunction     = memoize(
        (SchemafulTupleSet tuples) -> memoize(
            (List<String> factorNames) -> _uniqueTuples(tuples, factorNames)
        ));
    private final Function<Tuple, Function<Tuple, Tuple>>                                connect                  = memoize(
        (Tuple t) -> memoize((Tuple u) -> _connect(t, u)
        ));
    private final Function<SchemafulTupleSet, Function<Integer, TupleSet>>               uniqueTuplesOfStrength   = memoize(
        (SchemafulTupleSet tuples) -> memoize(
            (Integer strength) -> _uniqueTuplesOfStrength(tuples, strength)
        ));
    private final Function<SchemafulTupleSet, Function<List<String>, SchemafulTupleSet>> projectSchemafulTupleSet = memoize(
        tuples -> memoize(
            tuples::lenientProject
        ));

    TupleSet allPossibleUniqueTuplesOfStrength(
        SchemafulTupleSet lhs,
        SchemafulTupleSet rhs,
        List<String> alreadyProcessedFactorsInRhs,
        String newFactorNameInRhs,
        int strength
    ) {
      long before = System.currentTimeMillis();
      try {
        Checks.checkcond(strength > 1);
        Checks.checkcond(lhs.width() + alreadyProcessedFactorsInRhs.size() + 1 >= strength);
        return IntStream.range(
            1,
            strength
        ).filter(
            (int i) -> i + alreadyProcessedFactorsInRhs.size() + 1 >= strength
        ).mapToObj(
            (int i) -> uniqueTuplesOfStrength(lhs, i).cartesianProduct(
                new TupleSet.Builder().addAll(
                    stream(
                        new Combinator<>(alreadyProcessedFactorsInRhs, strength - i - 1).spliterator(),
                        false
                    ).flatMap(
                        (List<String> chosenFactorNames) -> uniqueTuples(
                            rhs,
                            concat(
                                chosenFactorNames.stream(),
                                Stream.of(newFactorNameInRhs)
                            ).collect(toList())
                        ).stream()
                    ).distinct(
                    ).collect(
                        toList()
                    )
                ).build()
            )
        ).reduce(
            (TupleSet t, TupleSet u) -> new TupleSet.Builder().addAll(t.toUnmodifiableCollection()).addAll(u.toUnmodifiableCollection()).build()
        ).orElseThrow(
            AssertionError::new
        );
      } finally {
        debug("allPossibleUniqueTuplesOfStrength:" + (System.currentTimeMillis() - before));
      }
    }

    TupleSet uniqueTuplesOfStrength(SchemafulTupleSet tuples, int strength) {
      return uniqueTuplesOfStrength.apply(tuples).apply(strength);
    }

    /*
     * This does exactly the same as what _uniqueTuples does but with better performance
     * by memoization.
     */
    TupleSet uniqueTuples(SchemafulTupleSet tuples, List<String> factorNames) {
      return uniqueTuplesFunction.apply(tuples).apply(factorNames);
    }

    Object chooseLevelThatCoversMostTuplesIn(Tuple τ, String f, TupleSet π, SchemafulTupleSet rhs, List<String> involvedFactors, List<List<String>> factorNameSets, Hg hg) {
      assert !τ.containsKey(f);
      Tuple q = project(
          τ,
          involvedFactors.subList(0, involvedFactors.size() - 1)
      );
      return Utils.max(
          rhs.project(involvedFactors).stream()
              .filter(q::isSubtupleOf)
              .map(tuple -> project(tuple, singletonList(f)))
              .distinct(),
          hg.max(),
          (Tuple t) -> (long) numberOfTuplesInπCoveredBy(connect(τ, t), π, factorNameSets)
      ).map(
          chosenTuple -> chosenTuple.get(f)
      ).orElseThrow(RuntimeException::new);
    }

    int numberOfTuplesInπCoveredBy(Tuple tuple, TupleSet π, List<List<String>> factorNameSets) {
      return (int) factorNameSets.stream()
          .mapToInt(factorNames -> π.contains(project(tuple, factorNames)) ? 1 : 0)
          .count();
    }

    Stream<List<String>> streamFactorNameSets(List<String> fromLhs, List<String> fromRhs, String f, int t) {
      return IntStream.range(1, t)
          .mapToObj((int i) -> new StreamableCombinator<>(fromLhs, i))
          .flatMap(StreamableCombinator::stream)
          .flatMap(
              (List<String> lhsFactors) -> combinations(fromRhs, t - lhsFactors.size() - 1)
                  .map((List<String> rhsFactors) -> (List<String>) new ArrayList<String>() {{
                    addAll(lhsFactors);
                    addAll(rhsFactors);
                    add(f);
                  }})
          );
    }

    Tuple chooseBestCombination(TupleSet π, SchemafulTupleSet lhs, SchemafulTupleSet rhs, List<String> involvedFactors, Vg vg) {
      class Entry {
        private final Tuple    tuple;
        private final TupleSet candidates;

        private Entry(Tuple tuple, TupleSet candidates) {
          this.tuple = tuple;
          this.candidates = candidates;
        }
      }
      Function<Tuple, Integer> countTuplesCoveredBy = memoize(
          tuple -> countTuplesCoveredBy(tuple, π)
      );
      SchemafulTupleSet rhsProjected = projectSchemafulTupleSet(rhs, involvedFactors);
      return Utils.max(
          lhs.stream().map(
              tuple -> new Entry(tuple, prune(rhsProjected, vg.usedRhsFor(tuple)))
          ),
          vg.maxCompatibleTuples,
          (Entry e) -> (long) countCompatibleTuples(e.tuple, π)
      ).filter(
          (Entry e) -> {
            vg.updateMaxCompatibleTuples(countCompatibleTuples(e.tuple, π));
            return true;
          }
      ).map(
          entry -> Utils.max(
              entry.candidates.stream(),
              vg.maxFor(entry.tuple),
              t -> (long) countTuplesCoveredBy.apply(connect(entry.tuple, t))
          ).filter(chosenFromCandidates -> {
                vg.updateMaxFor(entry.tuple, chosenFromCandidates, countTuplesCoveredBy.apply(connect(entry.tuple, chosenFromCandidates)));
                return true;
              }
          ).map(
              chosenFromCandidates -> connect(entry.tuple, chosenFromCandidates)
          ).orElseGet(() -> {
            throw new RuntimeException();
          })
      ).orElseGet(() -> {
        throw new RuntimeException();
      });
    }

    private TupleSet prune(SchemafulTupleSet rhsProjected, Set<Tuple> exclude) {
      return new TupleSet.Builder(
      ).addAll(
          rhsProjected.stream().filter(tuple -> !exclude.contains(tuple)).collect(toList())
      ).build();
    }

    static class Hg {
      long                       max         = Long.MAX_VALUE;
      Map<List<Object>, Integer> alreadyUsed = new HashMap<>();

      int howManyTimesAlreadyUsed(Object[] levels) {
        return alreadyUsed.getOrDefault(requireNonNull(asList(levels)), 0);
      }

      int howManyTimesAlreadyUsed(Tuple tuple, String[] F) {
        return howManyTimesAlreadyUsed(Arrays.stream(F).map(tuple::get).toArray());
      }

      void add(Object[] levels) {
        this.alreadyUsed.put(asList(levels), howManyTimesAlreadyUsed(levels) + 1);
      }

      long max() {
        return this.max;
      }

      void max(long max) {
        assert this.max >= max;
        this.max = max;
      }
    }

    static class Vg {
      private long                   maxCompatibleTuples = Long.MAX_VALUE;
      private Map<Tuple, Long>       maxForTuple         = new HashMap<>();
      private Map<Tuple, Set<Tuple>> usedRhs             = new HashMap<>();

      long maxFor(Tuple tuple) {
        return maxForTuple.getOrDefault(tuple, Long.MAX_VALUE);
      }

      void updateMaxFor(Tuple lhs, Tuple rhs, long value) {
        assert maxFor(lhs) >= value;
        this.maxForTuple.put(lhs, value);
        this.usedRhs.put(
            lhs,
            new HashSet<Tuple>() {{
              addAll(usedRhsFor(lhs));
              add(rhs);
            }}
        );
      }

      Set<Tuple> usedRhsFor(Tuple tuple) {
        return Collections.unmodifiableSet(usedRhs.getOrDefault(tuple, Collections.emptySet()));
      }

      void updateMaxCompatibleTuples(long maxCompatibleTuples) {
        assert this.maxCompatibleTuples >= maxCompatibleTuples;
        this.maxCompatibleTuples = maxCompatibleTuples;
      }
    }

    SchemafulTupleSet projectSchemafulTupleSet(SchemafulTupleSet tuples, List<String> factorNames) {
      return projectSchemafulTupleSet.apply(tuples).apply(factorNames);
    }

    Tuple connect(Tuple t, Tuple u) {
      return connect.apply(t).apply(u);
    }

    private int countCompatibleTuples(Tuple tuple, TupleSet π) {
      return (int) π.stream().filter(each -> areCompatible(tuple, each)).count();
    }

    private int countTuplesCoveredBy(Tuple t, TupleSet π) {
      return (int) π.stream(
      ).filter(
          tuple -> tuple.isSubtupleOf(t)
      ).count(
      );
    }

    private static boolean areCompatible(Tuple t, Tuple u) {
      return t.size() <= u.size() ?
          areCompatible_(t, u) :
          areCompatible_(u, t);
    }

    private static boolean areCompatible_(Tuple t, Tuple u) {
      for (String k : t.keySet()) {
        if (!u.containsKey(k))
          continue;
        if (!Objects.equals(t.get(k), u.get(k)))
          return false;
      }
      return true;
    }

    TupleSet.Builder ensureAllTuplesAreUsed(TupleSet.Builder ts, SchemafulTupleSet tuples) {
      tuples.stream()
          .filter(
              t -> ts.content().stream()
                  .map(tuple -> project(tuple, tuples.getAttributeNames()))
                  .noneMatch(t::equals)
          ).forEach(ts::add);
      return ts;
    }

    private static Tuple _connect(Tuple t, Tuple u) {
      return Tuple.builder().putAll(t).putAll(u).build();
    }

    TupleSet _uniqueTuplesOfStrength(SchemafulTupleSet tuples, int strength) {
      return new TupleSet.Builder().addAll(
          stream(
              new Combinator<>(
                  tuples.getAttributeNames(),
                  strength
              ).spliterator(),
              false
          ).flatMap(
              factorNames -> uniqueTuples(tuples, factorNames).stream()
          ).distinct(
          ).collect(
              toList()
          )
      ).build();
    }

    TupleSet _uniqueTuples(SchemafulTupleSet tuples, List<String> factorNames) {
      return new TupleSet.Builder().addAll(
          tuples.stream()
              .map(tuple -> project(tuple, factorNames))
              .distinct()
              .collect(toList())
      ).build();
    }
  }

  public static void debug(String s, Object... args) {
    if (StandardJoiner.isDebugEnabled())
      System.out.println(String.format("[%-8s] %s", Thread.currentThread().getName(), String.format(s, args)));
  }
}
