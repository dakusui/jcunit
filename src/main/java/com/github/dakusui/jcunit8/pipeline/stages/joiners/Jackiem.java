package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableCombinator;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.project;
import static com.github.dakusui.jcunit8.core.Utils.append;
import static com.github.dakusui.jcunit8.core.Utils.combinations;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

@SuppressWarnings("NonAsciiCharacters")
public class Jackiem extends Florence {
  private static final Object UNASSIGNED = new Object();

  public Jackiem(Requirement requirement) {
    super(requirement);
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    int t = this.requirement.strength();
    Session session = new Session();

    List<String> alreadyProcessedFactors = new LinkedList<>();
    Set<Tuple> used = new LinkedHashSet<>();
    TupleSet.Builder ts = new TupleSet.Builder().addAll(lhs);
    for (int i = 0; i < rhs.width(); i++) {
      debug("preparation:(begin)");
      long beforePreparation = currentTimeMillis();
      String F = rhs.getAttributeNames().get(i);
      TupleSet π = session.allPossibleUniqueTuplesOfStrength(
          lhs, rhs, alreadyProcessedFactors, F,
          t
      );
      final List<String> involvedFactors = append(alreadyProcessedFactors, singletonList(F));
      final List<List<String>> tWayFactorNameSets = session.streamFactorNameSets(
          lhs.getAttributeNames(),
          alreadyProcessedFactors,
          F,
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
            Object v = session.chooseLevelThatCoversMostTuplesIn(τ, F, π, rhs, involvedFactors, tWayFactorNameSets, hg);
            Tuple.Builder b = new Tuple.Builder() {{
              putAll(τ);
              put(F, v);
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
                lhs.getAttributeNames(), alreadyProcessedFactors, F, n.get(F), t, n
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

  private void updateBuilderAndMarkUsedIfUnique(Tuple.Builder b, Set<Tuple> used, List<Tuple> candidates) {
    if (candidates.size() == 1) {
      b.putAll(candidates.get(0));
      used.add(candidates.get(0));
    } else {
      List<Tuple> notUsedCandidates = (candidates.stream().filter(tuple -> !used.contains(tuple)).collect(toList()));
      if (notUsedCandidates.size() == 1) {
        b.putAll(notUsedCandidates.get(0));
        used.add(notUsedCandidates.get(0));
      }
    }
  }

  class Session extends Florence.Session {
    Tuple extendIfUnique(Tuple tuple, SchemafulTupleSet rhs) {
      List<Tuple> found = rhs.index().find(removeUnassigned(project(tuple, rhs.getAttributeNames())));
      return found.size() == 1 ?
          connect(tuple, found.get(0)) :
          tuple;
    }

    Tuple removeUnassigned(Tuple tuple) {
      return new Tuple.Builder() {{
        tuple.keySet().stream()
            .filter(key -> !Objects.equals(tuple.get(key), UNASSIGNED))
            .forEach(key -> tuple.put(key, tuple.get(key)));
      }}.build();
    }

    @Override
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

    private List<Tuple> tuplesNewlyCovered(List<String> factorsFromLhs, List<String> factorsFromRhs, String currentFactor, Object valueForCurrentFactor, int t, Tuple τ) {
      return combinations(
          append(factorsFromLhs, factorsFromRhs), t - 1
      ).map(factorNames -> new Tuple.Builder() {
        {
          factorNames.forEach(k -> put(k, τ.get(k)));
        }
      }.put(currentFactor, valueForCurrentFactor).build()).collect(toList());
    }
  }
}
