package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
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
import static com.github.dakusui.jcunit8.core.Utils.combinations;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.project;
import static java.util.Collections.singletonList;
import static java.util.Comparator.comparingInt;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("NonAsciiCharacters")
public class Florence extends Joiner.Base {
  private static boolean debug = false;
  private final Requirement requirement;

  public Florence(Requirement requirement) {
    this.requirement = requirement;
  }

  protected long sizeOf(SchemafulTupleSet tupleSet) {
    return -tupleSet.width();
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
      String F = rhs.getAttributeNames().get(i);
      TupleSet π = session.allPossibleUniqueTuplesOfStrength(
          lhs,
          rhs,
          alreadyProcessedFactors,
          F,
          t
      );
      ////
      // TODO: NOTE: Surprisingly, this optimization didn't help at all and expensive.
      //      removeAlreadyCoveredTuples(
      //          π,
      //          ts.content().stream().filter(tuple -> tuple.containsKey(F)).collect(toList()),
      //          t
      //      );
      final List<String> involvedFactors = concat(alreadyProcessedFactors.stream(), Stream.of(F)).collect(toList());
      final List<List<String>> tWayFactorNameSets = session
          .streamFactorNameSets(lhs.getAttributeNames(), alreadyProcessedFactors, F, t)
          .collect(toList());
      ////
      // hg
      long beforeHg = System.currentTimeMillis();
      int sizeOfπBeforeHd = π.size();
      try {
        for (Tuple τ : new ArrayList<>(ts.content())) {
          Object vi = session.chooseLevelThatCoversMostTuplesIn(τ, F, π, rhs, involvedFactors, tWayFactorNameSets);
          Tuple.Builder b = Tuple.builder().putAll(τ);
          List<Tuple> candidates = rhs.index().find(
              project(
                  b.put(F, vi).build(),
                  involvedFactors
              )
          );
          assert !candidates.isEmpty();
          if (candidates.size() == 1)
            b.putAll(candidates.get(0));
          else {
            List<Tuple> notUsedCandidates = (candidates.stream().filter(tuple -> !used.contains(tuple)).collect(toList()));
            if (notUsedCandidates.size() == 1) {
              b.putAll(notUsedCandidates.get(0));
              used.add(notUsedCandidates.get(0));
            }
          }
          π.removeAll(
              tuplesNewlyCovered(lhs.getAttributeNames(), alreadyProcessedFactors, F, vi, t, τ)
          );
          ts.remove(τ);
          ts.add(b.build());
        }
      } finally {
        debug("hg:" + π.size() + "<-" + sizeOfπBeforeHd + ":" + ts.content().size() + ":" + (System
            .currentTimeMillis() - beforeHg));
      }
      ////
      // vg
      long beforeVg = System.currentTimeMillis();
      try {
        int ii = 0;
        boolean firstTime = true;
        int tuplesRemovedFromπ = -1;
        while (!π.isEmpty()) {
          long beforeVg_i = System.currentTimeMillis();
          try {
            long max = tuplesRemovedFromπ < 0 ?
                new Combinator<>(involvedFactors, t).size() :
                tuplesRemovedFromπ;
            Tuple n = session.chooseBestCombination(
                π,
                lhs,
                rhs,
                involvedFactors,
                max
            );
            π.removeAll(TupleUtils.subtuplesOf(n, t));
            ts.add(n);
          } finally {
            firstTime = false;
            debug("vg[%s]:%s:%s:%s", ii, π.size(), ts.content().size(), (System.currentTimeMillis() - beforeVg_i));
            if (π.size() < 16) {
              debug("π=%s", π);
            }
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

  private void removeAlreadyCoveredTuples(TupleSet π, Collection<Tuple> ts, int t) {
    ts.forEach(
        tuple -> π.removeAll(TupleUtils.subtuplesOf(tuple, t))
    );
  }

  private List<Tuple> tuplesNewlyCovered(List<String> factorsFromLhs, List<String> factorsFromRhs, String currentFactor, Object valueForCurrentFactor, int t, Tuple τ) {
    return stream(new Combinator<>(
            concat(
                factorsFromLhs.stream(),
                factorsFromRhs.stream()
            ).collect(toList()), t - 1).spliterator(),
        false
    ).map(factorNames -> new Tuple.Builder() {
      {
        factorNames.forEach(k -> put(k, τ.get(k)));
      }
    }.put(currentFactor, valueForCurrentFactor).build()).collect(toList());
  }

  private static class Session {
    private final Function<SchemafulTupleSet, Function<List<String>, TupleSet>> uniqueTuplesFunction = memoize(
        (SchemafulTupleSet tuples) -> memoize(
            (List<String> factorNames) -> _uniqueTuples(tuples, factorNames)
        ));

    private TupleSet allPossibleUniqueTuplesOfStrength(
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
            (TupleSet t, TupleSet u) -> new TupleSet.Builder().addAll(t).addAll(u).build()
        ).orElseThrow(
            AssertionError::new
        );
      } finally {
        debug("allPossibleUniqueTuplesOfStrength:" + (System.currentTimeMillis() - before));
      }
    }

    private TupleSet uniqueTuplesOfStrength(SchemafulTupleSet tuples, int strength) {
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

    /*
     * This does exactly the same as what _uniqueTuples does but with better performance
     * by memoization.
     */
    private TupleSet uniqueTuples(SchemafulTupleSet tuples, List<String> factorNames) {
      return uniqueTuplesFunction.apply(tuples).apply(factorNames);
    }

    private TupleSet _uniqueTuples(SchemafulTupleSet tuples, List<String> factorNames) {
      return new TupleSet.Builder().addAll(
          tuples.stream()
              .map(tuple -> project(tuple, factorNames))
              .distinct()
              .collect(toList())
      ).build();
    }

    Object chooseLevelThatCoversMostTuplesIn(Tuple τ, String f, TupleSet π, SchemafulTupleSet rhs, List<String> involvedFactors, List<List<String>> factorNameSets) {
      if (τ.containsKey(f))
        return τ.get(f);
      Tuple q = project(
          τ,
          involvedFactors.subList(0, involvedFactors.size() - 1)
      );
      return rhs.project(involvedFactors).stream()
          .filter(q::isSubtupleOf)
          .map(tuple -> project(tuple, singletonList(f)))
          .distinct()
          .max(comparingInt(o -> numberOfTuplesCoveredBy(τ, f, o, π, factorNameSets)))
          .map(
              chosenTuple -> chosenTuple.get(f)
          )
          .orElseThrow(RuntimeException::new);
    }

    private int numberOfTuplesCoveredBy(Tuple τ, String f, Object v, TupleSet π, List<List<String>> factorNameSets) {
      Tuple tuple = Tuple.builder().putAll(τ).put(f, v).build();
      return (int) factorNameSets.stream()
          .mapToInt(factorNames -> π.contains(project(factorNames, tuple)) ? 1 : 0)
          .count();
    }

    Stream<List<String>> streamFactorNameSets(List<String> fromLhs, List<String> fromRhs, String f, int t) {
      return IntStream.range(1, t)
          .filter((int i) -> i > fromLhs.size())
          .filter((int i) -> t - i - 1 > fromLhs.size())
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

    Tuple chooseBestCombination(TupleSet π, SchemafulTupleSet lhs, SchemafulTupleSet rhs, List<String> involvedFactors, long max) {
      class Entry {
        private final Tuple    tuple;
        private final TupleSet candidates;

        private Entry(Tuple tuple, TupleSet candidates) {
          this.tuple = tuple;
          this.candidates = candidates;
        }
      }
      return lhs.stream().map(
          tuple -> new Entry(tuple, simplify(rhs, involvedFactors))
      ).max(comparingInt(
          o -> countOverlappingTuples(o.tuple, π))
      ).map(
          entry -> Utils.max(
              entry.candidates.stream(),
              max,
              t -> (long) countTuplesCoveredBy(t, entry.tuple, π)
          ).map(
              chosenFromCandidates -> connect(entry.tuple, chosenFromCandidates)
          ).orElseGet(() -> {
            // workaround compilation error on intellij ultimate/macosx
            throw new RuntimeException();
          })
      ).orElseGet(() -> {
        // workaround compilation error on intellij ultimate/macosx
        throw new RuntimeException();
      });
      /*
      return lhs.stream().map(
          tuple -> new Entry(tuple, simplify(rhs, involvedFactors))
      ).max(comparingInt(
          o -> countOverlappingTuples(o.tuple, π))
      ).map(
          entry -> entry.candidates.stream()
              .max(comparingInt(t -> countTuplesCoveredBy(t, entry.tuple, π)))
              .map(chosenFromCandidates -> connect(entry.tuple, chosenFromCandidates))
              .orElseGet(() -> {
                // workaround compilation error on intellij ultimate/macosx
                throw new RuntimeException();
              })
      ).orElseGet(() -> {
        // workaround compilation error on intellij ultimate/macosx
        throw new RuntimeException();
      });
      */
    }

    private TupleSet simplify(SchemafulTupleSet in, List<String> involvedFactors) {
      return new TupleSet.Builder() {
        {
          in.stream().map(tuple -> {
            Tuple q = project(tuple, involvedFactors);
            return in.index().find(q).size() == 1 ?
                tuple :
                q;
          }).distinct().forEach(this::add);
        }
      }.build();
    }

    private Tuple connect(Tuple t, Tuple u) {
      return Tuple.builder().putAll(t).putAll(u).build();
    }

    private int countOverlappingTuples(Tuple tuple, TupleSet π) {
      return (int) π.stream()
          .filter(each ->
              //              disjoint(each.keySet(), tuple.keySet()) ||
              intersection(tuple, each).isPresent())
          .count();
    }

    private int countTuplesCoveredBy(Tuple t, Tuple u, TupleSet π) {
      return (int) π.stream(
      ).filter(
          tuple -> tuple.isSubtupleOf(connect(t, u))
      ).count(
      );
    }

    private static Optional<Tuple> intersection(Tuple t, Tuple u) {
      return t.size() <= u.size() ?
          intersections_(t, u) :
          intersections_(u, t);
    }

    private static Optional<Tuple> intersections_(Tuple t, Tuple u) {
      Tuple.Builder b = Tuple.builder();
      for (String k : t.keySet()) {
        if (!u.containsKey(k))
          continue;
        if (!Objects.equals(u.get(k), t.get(k)))
          return Optional.empty();
        b.put(k, t.get(k));
      }
      return b.isEmpty() ?
          Optional.empty() :
          Optional.of(b.build());
    }

    TupleSet.Builder ensureAllTuplesAreUsed(TupleSet.Builder ts, SchemafulTupleSet tuples) {
      tuples.stream()
          .filter(
              t -> ts.content().stream()
                  .map(tuple -> project(tuples.getAttributeNames(), tuple))
                  .noneMatch(t::equals)
          ).forEach(ts::add);
      return ts;
    }
  }

  static void debug(String s, Object... args) {
    if (StandardJoiner.isDebugEnabled())
      System.out.println(String.format(s, args));
  }
}
