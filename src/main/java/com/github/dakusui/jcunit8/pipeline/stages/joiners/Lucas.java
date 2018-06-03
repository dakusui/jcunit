package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.project;
import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit8.core.Utils.*;
import static java.lang.Math.min;
import static java.lang.System.currentTimeMillis;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;
import static java.util.stream.IntStream.range;

@SuppressWarnings("NonAsciiCharacters")
public class Lucas extends Florence {
  private final int initialDelta;

  public Lucas(Requirement requirement) {
    super(requirement);
    initialDelta = requirement.strength() * 2;
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    int t = this.requirement.strength();
    Session session = new Session();

    List<String> alreadyProcessedFactors = new LinkedList<>();
    Set<Tuple> used = new LinkedHashSet<>();
    TupleSet.Builder ts = new TupleSet.Builder().addAll(lhs);
    int delta = initialDelta;
    for (int i = 0; i < rhs.width(); i += delta) {
      debug("preparation:(begin)");
      long beforePreparation = currentTimeMillis();
      String[] F = sublist(rhs.getAttributeNames(), i, i + delta).toArray(new String[0]);
      TupleSet π = session.allPossibleUniqueTuplesOfStrength(
          lhs, rhs, alreadyProcessedFactors, F,
          t,
          session.precovered(ts, lhs.getAttributeNames(), alreadyProcessedFactors, asList(F), t)
      );
      final List<String> involvedFactors = append(alreadyProcessedFactors, asList(F));
      final List<List<String>> tWayFactorNameSets = session.streamFactorNameLists(
          lhs.getAttributeNames(),
          alreadyProcessedFactors,
          asList(F),
          t
      ).collect(toList());
      debug("preparation:π.size=%s,ts.size=%s:%s[msec]", π.size(), ts.content().size(), currentTimeMillis() - beforePreparation);
      ////
      // hg
      debug("hg:(begin)");
      long beforeHg = currentTimeMillis();
      try {
        Florence.Session.Hg hg = new Florence.Session.Hg();
        int ii = 0;
        for (Tuple τ : new ArrayList<>(ts.content())) {
          long beforeHg_i = currentTimeMillis();
          try {
            hg.max(Math.min(
                tWayFactorNameSets.size(),
                π.size()
            ));
            Object[] v = session.chooseLevelsThatCoverMostTuplesInπ(τ, F, π, rhs, involvedFactors, tWayFactorNameSets, hg);
            assert v.length == F.length;
            Tuple.Builder b = new Tuple.Builder() {{
              putAll(τ);
              IntStream.range(0, v.length).forEach(ii -> put(F[ii], v[ii]));
            }};
            List<Tuple> candidates = rhs.index().find(
                project(b.build(), involvedFactors)
            );
            assert !candidates.isEmpty();
            updateBuilderAndMarkUsedIfUnique(b, used, candidates);
            π.removeAll(
                session.tuplesNewlyCovered(lhs.getAttributeNames(), alreadyProcessedFactors, F, v, t, τ)
            );
            ts.remove(τ);
            ts.add(b.build());
          } finally {
            debug("  hg[%s]:π.size=%s,ts.size=%s:%s[msec]",
                ii++, π.size(), ts.content().size(), currentTimeMillis() - beforeHg_i);
          }
        }
      } finally {
        debug("hg:π.size=%s,ts.size=%s:%s[msec]",
            π.size(), ts.content().size(), currentTimeMillis() - beforeHg);
      }
      ////
      // vg
      debug("vg:(begin)");
      long beforeVg = currentTimeMillis();
      try {
        int ii = 0;
        Florence.Session.Vg vg = new Florence.Session.Vg();
        while (!π.isEmpty()) {
          long beforeVg_i = currentTimeMillis();
          try {
            Tuple n = session.extendIfUnique(
                session.chooseBestCombination(
                    π,
                    lhs, rhs, involvedFactors,
                    vg
                ),
                rhs
            );
            π.removeAll(session.tuplesNewlyCovered(
                lhs.getAttributeNames(), alreadyProcessedFactors, F, levelsFor(F, n), t, n
            ));
            ts.add(n);
          } finally {
            debug("  vg[%s]:π.size=%s,ts.size=%s:%s[msec]", ii, π.size(), ts.content().size(), (currentTimeMillis() - beforeVg_i));
            ii++;
          }
        }
      } finally {
        debug("vg:π.size=%s,ts.size=%s:%s[msec]", π.size(), ts.content().size(), currentTimeMillis() - beforeVg);
      }
      alreadyProcessedFactors = involvedFactors;
      //delta = 2;
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

  private Object[] levelsFor(String[] f, Tuple tuple) {
    return Arrays.stream(f).map(tuple::get).toArray();
  }

  private void updateBuilderAndMarkUsedIfUnique(Tuple.Builder b, Set<Tuple> used, List<Tuple> candidates) {
    if (candidates.size() == 1) {
      Tuple it = candidates.get(0);
      b.putAll(it);
      if (!used.contains(it))
        used.add(candidates.get(0));
    } else {
      List<Tuple> notUsedCandidates = (candidates.stream().filter(tuple -> !used.contains(tuple)).collect(toList()));
      if (notUsedCandidates.size() == 1) {
        Tuple it = notUsedCandidates.get(0);
        b.putAll(it);
        if (!used.contains(it))
          used.add(it);
      }
    }
  }

  public static class Session extends Florence.Session {
    private final Function<List<String>, Function<List<String>, Function<List<String>, Function<Integer, List<List<String>>>>>>
                                                                                                    listFactorNameLists                = memoize(
        lhsFactorNames -> memoize(
            rhsFactorNames -> memoize(
                cur -> memoize(
                    t -> listFactorNameLists_(lhsFactorNames, rhsFactorNames, cur, t)
                ))));
    private final Function<SchemafulTupleSet, Function<List<String>, Function<Tuple, List<Tuple>>>> computePossiblePartialTupleFromRhs = memoize(
        rhs -> memoize(
            involvedFactors -> memoize(
                q -> computePossiblePartialTupleFromRhs_(rhs, involvedFactors, q)))
    );


    TupleSet allPossibleUniqueTuplesOfStrength(
        SchemafulTupleSet lhs,
        SchemafulTupleSet rhs,
        List<String> alreadyProcessedFactorsInRhs,
        String[] newFactorNamesInRhs,
        int strength,
        Set<Tuple> precovered
    ) {
      long before = currentTimeMillis();
      try {
        checkcond(strength > 1);
        checkcond(lhs.width() + alreadyProcessedFactorsInRhs.size() + 1 >= strength);
        return new TupleSet.Builder().addAll(
            streamFactorNameLists(
                lhs.getAttributeNames(),
                alreadyProcessedFactorsInRhs,
                asList(newFactorNamesInRhs),
                strength
            ).flatMap(
                chosenFactorNames -> projectSchemafulTupleSet(lhs, chosenFactorNames)
                    .stream()
                    .parallel()
                    .flatMap(
                        fromLhs -> projectSchemafulTupleSet(rhs, chosenFactorNames)
                            .stream()
                            .parallel().map(
                                fromRhs -> connect(fromLhs, fromRhs)
                            ))
            ).filter(
                tuple -> !precovered.contains(tuple)
            ).collect(toSet())
        ).build();
      } finally {
        debug("  allPossibleUniqueTuplesOfStrength::%s[msec]", currentTimeMillis() - before);
      }
    }

    Set<Tuple> precovered(TupleSet.Builder ongoing, List<String> lhsFactors, List<String> factorsFromRhsAlreadyProcessed, List<String> factorsFromRhsBeingProcessed, int t) {
      long before = currentTimeMillis();
      try {
        List<Tuple> preextendedTuples = ongoing.content().stream().filter(
            tuple -> tuple.containsKey(factorsFromRhsBeingProcessed.get(0))
        ).collect(toList());
        if (preextendedTuples.isEmpty())
          return Collections.emptySet();
        return streamFactorNameLists(lhsFactors, factorsFromRhsAlreadyProcessed, factorsFromRhsBeingProcessed, t)
            .parallel()
            .flatMap(factors -> preextendedTuples.stream().parallel().map(
                tuple -> project(tuple, factors)
            )).collect(toSet());
      } finally {
        debug("  precovered::%s[msec]", currentTimeMillis() - before);
      }
    }

    Stream<List<String>> streamFactorNameLists(List<String> lhsFactorNames, List<String> rhsFactorNames, List<String> cur, int t) {
      return listFactorNameLists.apply(lhsFactorNames).apply(rhsFactorNames).apply(cur).apply(t).stream();
    }

    List<List<String>> listFactorNameLists_(List<String> lhsFactorNames, List<String> rhsFactorNames, List<String> cur, int t) {
      long before = currentTimeMillis();
      try {
        return streamFactorNameLists_(lhsFactorNames, rhsFactorNames, cur, t).collect(toList());
      } finally {
        debug("  --listFactorNameLists_::%s[msec]", currentTimeMillis() - before);
      }
    }

    @SuppressWarnings("unchecked")
    Stream<List<String>> streamFactorNameLists_(List<String> lhsFactorNames, List<String> rhsFactorNames, List<String> cur, int t) {
      // min(lhs.size, t - 1) >= #(from lhs)              >= 1
      // min(rhs.size, t - 1) >= #(from rhs (processed))  >= 0
      // min(F.length, t - 1)>= #(from F[] (new))        >= 1
      // #(from rhs) + #(from F[]) + #(from lhs) == t
      return cartesian(
          range(1, min(lhsFactorNames.size() + 1, t)).boxed(),
          range(1, min(cur.size() + 1, t)).boxed()
      ).map(
          degrees -> new ArrayList<Integer>(3) {{
            addAll(degrees);
            add(t - get(0) - get(1));
          }}
      ).filter(
          degrees -> degrees.stream().allMatch(d -> d >= 0)
      ).flatMap(degrees -> cartesian(
          combinations(lhsFactorNames, degrees.get(0)),
          combinations(rhsFactorNames, degrees.get(2)),
          combinations(cur, degrees.get(1))
      )).map(
          factorNames -> append(factorNames.get(0), append(factorNames.get(1), factorNames.get(2)))
      );
    }

    Object[] chooseLevelsThatCoverMostTuplesInπ(
        /* A tuple to be grown horizontally with levels returned by this method.*/
        Tuple τ,
        String[] F, TupleSet π, SchemafulTupleSet rhs, List<String> involvedFactors, List<List<String>> factorNameSets, Hg hg) {
      Tuple q = project(τ, involvedFactors);
      return Utils.max(
          computePossiblePartialTupleFromRhs(rhs, involvedFactors, q)
              .stream()
              .sorted(Comparator.comparingInt(t -> hg.howManyTimesAlreadyUsed(t, F)))
              .map(tuple -> project(tuple, asList(F)))
              .distinct(),
          hg.max(),
          (Tuple t) -> (long) numberOfTuplesInπCoveredBy(connect(τ, t), π, factorNameSets)
      ).map(
          chosenTuple -> Arrays.stream(F).map(chosenTuple::get).collect(toList()).toArray()
      ).filter(
          levels -> {
            hg.add(levels);
            return true;
          }
      ).orElseThrow(
          AssertionError::new
      );
    }

    private List<Tuple> computePossiblePartialTupleFromRhs(SchemafulTupleSet rhs, List<String> involvedFactors, Tuple q) {
      return computePossiblePartialTupleFromRhs.apply(rhs).apply(involvedFactors).apply(q);
    }

    private List<Tuple> computePossiblePartialTupleFromRhs_(SchemafulTupleSet rhs, List<String> involvedFactors, Tuple q) {
      return projectSchemafulTupleSet(rhs, involvedFactors).stream()
          .filter(q::isSubtupleOf)
          .distinct()
          .collect(toList());
    }

    List<Tuple> tuplesNewlyCovered(List<String> factorsFromLhs, List<String> factorsFromRhs, String[] currentFactors, Object[] valuesForCurrentFactors, int t, Tuple τ) {
      assert currentFactors.length == valuesForCurrentFactors.length;
      Tuple base = new Tuple.Builder() {{
        putAll(τ);
        IntStream.range(0, currentFactors.length).forEach(i -> put(currentFactors[i], valuesForCurrentFactors[i]));
      }}.build();
      return streamFactorNameLists(factorsFromLhs, factorsFromRhs, asList(currentFactors), t)
          .map(factorNames -> project(base, factorNames))
          .collect(toList());
    }

    Tuple extendIfUnique(Tuple tuple, SchemafulTupleSet rhs) {
      List<Tuple> found = rhs.index().find(project(tuple, rhs.getAttributeNames()));
      return found.size() == 1 ?
          connect(tuple, found.get(0)) :
          tuple;
    }
  }
}
