package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit8.core.Utils.*;
import static java.lang.Math.min;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static java.util.stream.Stream.concat;
import static java.util.stream.StreamSupport.stream;

@SuppressWarnings("NonAsciiCharacters")
public class Lucas extends Florence {
  public Lucas(Requirement requirement) {
    super(requirement);
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    int t = this.requirement.strength();
    Session session = new Session();

    List<String> alreadyProcessedFactors = new LinkedList<>();
    Set<Tuple> used = new LinkedHashSet<>();
    TupleSet.Builder ts = new TupleSet.Builder().addAll(lhs);
    for (int i = 0; i < rhs.width(); i += t - 1) {
      String[] F = sublist(rhs.getAttributeNames(), i, i + t - 1).toArray(new String[0]);
      TupleSet π = session.allPossibleUniqueTuplesOfStrength(
          lhs,
          rhs,
          alreadyProcessedFactors,
          F,
          t
      );
      final List<String> involvedFactors = concat(alreadyProcessedFactors.stream(), Stream.of(F)).collect(toList());
      final List<List<String>> tWayFactorNameSets = session
          .chooseFactorNames(lhs.getAttributeNames(), alreadyProcessedFactors, asList(F), t)
          .collect(toList());
      ////
      // hg
      long beforeHg = System.currentTimeMillis();
      int sizeOfπBeforeHg = π.size();
      try {

        int tuplesRemovedLastTime = -1;
        for (Tuple τ : new ArrayList<>(ts.content())) {
          int sizeOfπBeforeRemoval = π.size();
          long max = Math.min(
              tuplesRemovedLastTime == -1 ?
                  Long.MAX_VALUE :
                  tuplesRemovedLastTime,
              sizeOfπBeforeHg
          );
          Object vi = session.chooseLevelThatCoversMostTuplesIn(τ, F[0], π, rhs, involvedFactors, tWayFactorNameSets, max);
          Tuple.Builder b = Tuple.builder().putAll(τ);
          List<Tuple> candidates = rhs.index().find(
              project(
                  involvedFactors,
                  b.put(F[0], vi).build()
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
              tuplesNewlyCovered(lhs.getAttributeNames(), alreadyProcessedFactors, F[0], vi, t, τ)
          );
          tuplesRemovedLastTime = sizeOfπBeforeRemoval - π.size();
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
        int tuplesRemovedLastTime = -1;
        while (!π.isEmpty()) {
          long beforeVg_i = System.currentTimeMillis();
          try {
            int sizeOfπBeforeRemoval = π.size();
            long max = Math.min(
                tuplesRemovedLastTime < 0 ?
                    Long.MAX_VALUE :
                    tuplesRemovedLastTime,
                π.size()
            );
            Tuple n = session.chooseBestCombination(
                π,
                lhs,
                rhs,
                involvedFactors,
                max
            );
            π.removeAll(TupleUtils.subtuplesOf(n, t));
            ts.add(n);
            tuplesRemovedLastTime = sizeOfπBeforeRemoval - π.size();
          } finally {
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

  private static class Session extends Florence.Session {
    private final Function<SchemafulTupleSet, Function<List<String>, TupleSet>> uniqueTuplesFunction = memoize(
        (SchemafulTupleSet tuples) -> memoize(
            (List<String> factorNames) -> _uniqueTuples(tuples, factorNames)
        ));

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
        checkcond(newFactorNamesInRhs.length == strength - 1);

        // lhs              >= 1
        // rhs (processed)  >= 0
        // F[] (new)        >= 1
        // #(from rhs) + #(from F[]) + #(from lhs) == t
        return range(
            1,
            strength
        ).filter(
            (int i) -> i + alreadyProcessedFactorsInRhs.size() + 1 >= strength
        ).mapToObj(
            (int i) -> uniqueTuplesOfStrength(lhs, i).cartesianProduct(
                new TupleSet.Builder().addAll(
                    stream(
                        new Combinator<>(alreadyProcessedFactorsInRhs, strength - i - strength - 1).spliterator(),
                        false
                    ).flatMap(
                        (List<String> chosenFactorNames) -> uniqueTuples(
                            rhs,
                            concat(
                                chosenFactorNames.stream(),
                                Arrays.stream(newFactorNamesInRhs)
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

    @SuppressWarnings("unchecked")
    private Stream<List<String>> chooseFactorNames(List<String> lhsFactorNames, List<String> rhsFactorNames, List<String> cur, int t) {
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
      ).flatMap(degrees -> cartesian(
          combinations(lhsFactorNames, degrees.get(0)),
          combinations(rhsFactorNames, degrees.get(2)),
          combinations(cur, degrees.get(1))
      )).map(
          factorNames -> append(factorNames.get(0), append(factorNames.get(1), factorNames.get(2)))
      );
    }

    private static <T> List<T> append(List<T> a, List<T> b) {
      return new ArrayList<T>(a.size() + b.size()) {{
        addAll(a);
        addAll(b);
      }};
    }
  }

  public static void main(String... args) {
    new Session().chooseFactorNames(
        asList("l0", "l1", "l2"),
        asList("r0", "r1", "r2"),
        asList("c0", "c1", "c2"),
        3
    ).forEach(System.out::println);
  }

  private static <T> List<T> sublist(List<T> list, int fromIndex, int toIndex) {
    return list.subList(
        fromIndex,
        toIndex < list.size() ?
            toIndex :
            list.size()
    );
  }
}
