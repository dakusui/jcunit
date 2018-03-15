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
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

@SuppressWarnings("NonAsciiCharacters")
public class Lucas extends Florence {
  private final int delta;

  public Lucas(Requirement requirement) {
    super(requirement);
    delta = requirement.strength() * 2;
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    int t = this.requirement.strength();
    Session session = new Session();

    List<String> alreadyProcessedFactors = new LinkedList<>();
    Set<Tuple> used = new LinkedHashSet<>();
    TupleSet.Builder ts = new TupleSet.Builder().addAll(lhs);
    for (int i = 0; i < rhs.width(); i += delta) {
      String[] F = sublist(rhs.getAttributeNames(), i, i + delta).toArray(new String[0]);
      TupleSet π = session.allPossibleUniqueTuplesOfStrength(
          lhs,
          rhs,
          alreadyProcessedFactors,
          F,
          t
      );
      final List<String> involvedFactors = append(alreadyProcessedFactors, asList(F));
      final List<List<String>> tWayFactorNameSets = session.streamFactorNameLists(
          lhs.getAttributeNames(),
          alreadyProcessedFactors,
          asList(F),
          t
      ).collect(toList());
      ////
      // hg
      long beforeHg = System.currentTimeMillis();
      int sizeOfπBeforeHg = π.size();
      try {
        Florence.Session.Hg hg = new Florence.Session.Hg();
        for (Tuple τ : new ArrayList<>(ts.content())) {
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
              session.tuplesNewlyCovered(lhs.getAttributeNames(), alreadyProcessedFactors, F, v, t, τ)
          );
          ts.remove(τ);
          ts.add(b.build());
        }
      } finally {
        debug("hg:" + π.size() + "<-" + sizeOfπBeforeHg + ":" + ts.content().size() + ":" + (System
            .currentTimeMillis() - beforeHg));
      }
      ////
      // vg
      long beforeVg = System.currentTimeMillis();
      try {
        int ii = 0;
        Florence.Session.Vg vg = new Florence.Session.Vg();
        while (!π.isEmpty()) {
          long beforeVg_i = System.currentTimeMillis();
          try {
            Tuple n = session.chooseBestCombination(
                π,
                lhs,
                rhs,
                involvedFactors,
                vg
            );
            π.removeAll(session.tuplesNewlyCovered(
                lhs.getAttributeNames(), alreadyProcessedFactors, F, Arrays.stream(F).map(n::get).toArray(), t, n
            ));
            ts.add(n);
          } finally {
            debug("vg[%s]:%s:%s:%s", ii, π.size(), ts.content().size(), (System.currentTimeMillis() - beforeVg_i));
            if (π.size() < 10) {
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

  public static class Session extends Florence.Session {
    private final Function<List<String>, Function<List<String>, Function<List<String>, Function<Integer, List<List<String>>>>>>
        listFactorNameLists_ = memoize(
        lhsFactorNames -> memoize(
            rhsFactorNames -> memoize(
                cur -> memoize(
                    t -> listFactorNameLists_(lhsFactorNames, rhsFactorNames, cur, t)
                ))));

    TupleSet allPossibleUniqueTuplesOfStrength(
        SchemafulTupleSet lhs,
        SchemafulTupleSet rhs,
        List<String> alreadyProcessedFactorsInRhs,
        String[] newFactorNamesInRhs,
        int strength
    ) {
      long before = System.currentTimeMillis();
      try {
        checkcond(strength > 1);
        checkcond(lhs.width() + alreadyProcessedFactorsInRhs.size() + 1 >= strength);
        return new TupleSet.Builder().addAll(streamFactorNameLists(
            lhs.getAttributeNames(),
            alreadyProcessedFactorsInRhs,
            asList(newFactorNamesInRhs),
            strength
            ).flatMap(
            chosenFactorNames -> lhs.lenientProject(chosenFactorNames).stream().flatMap(
                fromLhs -> rhs.lenientProject(chosenFactorNames).stream().map(
                    fromRhs -> connect(fromLhs, fromRhs)))
            ).collect(toList())
        ).build();
      } finally {
        debug("allPossibleUniqueTuplesOfStrength:%s", (System.currentTimeMillis() - before));
      }
    }

    Stream<List<String>> streamFactorNameLists(List<String> lhsFactorNames, List<String> rhsFactorNames, List<String> cur, int t) {
      return listFactorNameLists_.apply(lhsFactorNames).apply(rhsFactorNames).apply(cur).apply(t).stream();
    }

    List<List<String>> listFactorNameLists_(List<String> lhsFactorNames, List<String> rhsFactorNames, List<String> cur, int t) {
      return streamFactorNameLists_(lhsFactorNames, rhsFactorNames, cur, t).collect(toList());
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

    Object[] chooseLevelsThatCoverMostTuplesInπ(Tuple τ, String[] F, TupleSet π, SchemafulTupleSet rhs, List<String> involvedFactors, List<List<String>> factorNameSets, Hg hg) {
      Tuple q = project(
          τ,
          involvedFactors.subList(0, involvedFactors.size() - 1)
      );
      return Utils.max(
          rhs.project(involvedFactors).stream()
              .filter(q::isSubtupleOf)
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
  }
}
