package com.github.dakusui.jcunit8.pipeline.stages.joiners.old;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.core.StreamableCartesianator;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.*;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.sizeOfIntersection;
import static com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner.findCoveringTuplesIn;
import static com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner.log;
import static java.lang.Math.max;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

@SuppressWarnings("NonAsciiCharacters")
public class IncrementalJoiner extends Joiner.Base {

  private final Requirement requirement;

  public IncrementalJoiner(Requirement requirement) {
    this.requirement = requirement;
  }

  @Override
  protected SchemafulTupleSet doJoin(final SchemafulTupleSet lhs, final SchemafulTupleSet rhs) {
    Session session = new Session(requirement, lhs, rhs);
    log("Inc:phase-0: incremental-join started");
    List<Tuple> ts = buildInitialTupleSet(requirement, lhs, rhs);
    log("Inc:phase-1: ts(init)=%s", ts.size());

    final int t = requirement.strength();
    final int n = lhs.width();

    List<String> processedFactors = new ArrayList<String>(lhs.getAttributeNames().size() - t) {{
      addAll(lhsAttributeNamesInSeeds(requirement, lhs));
    }};
    for (int i = t; i < n; i++) {
      String Pi = lhs.getAttributeNames().get(i);
      log("Inc:phase-2.1: Pi=%s, ts.size=%s", Pi, ts.size());
      @SuppressWarnings("NonAsciiCharacters") TupleSet π = prepare_π(t, Pi, processedFactors, lhs, rhs);
      removeTuplesCoveredByExtendedTuplesFrom_π(t, π, ts, Pi, processedFactors, rhs);
      log("Inc:phase-2.2a: π=%s", π.size());
      // hg
      ts = session.hg(lhs, rhs, t, Pi, processedFactors, ts, π);
      log("Inc:phase-2.2b: ts.size=%s: π=%s", π.size(), ts.size());
      ts = extendByLhsWherePossible(ts, lhs);
      processedFactors.add(Pi);
      while (!π.isEmpty()) {
        log("Inc:phase-2.2.1: π=%s", π.size());
        session.findBestCombinationsFor(
            π.stream().findFirst().orElseThrow(
                IllegalStateException::new
            ),
            ts,
            π
        );
      }
      log("Inc:phase-2.3: ts.size=%s", ts.size());
    }
    ensureAllTuplesAreUsed(ts, lhs, rhs);
    log("Inc:phase-3: ts.size=%s", ts.size());
    return new SchemafulTupleSet.Builder(
        concat(lhs.getAttributeNames().stream(), rhs.getAttributeNames().stream()).collect(toList())
    ).addAllEntries(
        ts
    ).build();
  }

  private static SchemafulTupleSet buildInitialTupleSet(Requirement requirement, SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    List<String> lhsAttributeNamesInSeeds = lhsAttributeNamesInSeeds(requirement, lhs);
    return new StandardJoiner(requirement).apply(
        lhs.project(lhsAttributeNamesInSeeds), rhs
    );
  }

  private static List<String> lhsAttributeNamesInSeeds(Requirement requirement, SchemafulTupleSet lhs) {
    return lhs.getAttributeNames().subList(0, requirement.strength());
  }

  @SuppressWarnings({ "unchecked", "NonAsciiCharacters" })
  private static TupleSet prepare_π(int strength, String pi, List<String> processedFactors, SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    return new TupleSet.Builder().addAll(
        streamInvolvedFactorNames(strength, pi, processedFactors, rhs.getAttributeNames()).flatMap(
            involvedFactorNames -> new StreamableCartesianator<>(
                lhs.index().allPossibleTuples(involvedFactorNames.stream().filter(lhs.index()::hasAttribute).collect(toList())),
                rhs.index().allPossibleTuples(involvedFactorNames.stream().filter(rhs.index()::hasAttribute).collect(toList()))
            ).stream()
        ).map(
            tuples -> TupleUtils.connect(tuples.get(0), tuples.get(1))
        ).collect(
            toList()
        )
    ).build();
  }

  private static List<Tuple> extendByLhsWherePossible(List<Tuple> ts, SchemafulTupleSet lhs) {
    return ts.stream().map(
        tuple -> {
          List<Tuple> candidates = lhs.index().find(project(tuple, lhs.getAttributeNames()));
          assert !candidates.isEmpty();
          return candidates.size() == 1 ?
              connect(candidates.get(0), tuple) :
              tuple;
        }
    ).collect(toList());
  }

  private static void removeTuplesCoveredByExtendedTuplesFrom_π(int strength, TupleSet π, List<Tuple> ts, String Pi, List<String> processedFactors, SchemafulTupleSet rhs) {
    List<List<String>> involvedFactors = involvedFactorNames(strength, Pi, processedFactors, rhs.getAttributeNames());
    ts.stream().filter(
        tuple -> tuple.size() > processedFactors.size() + rhs.width()
    ).flatMap(
        (Function<Tuple, Stream<Tuple>>) tuple -> involvedFactors.stream().map(
            involved -> project(tuple, involved))
    ).forEach(
        π::remove
    );
  }

  private static void ensureAllTuplesAreUsed(List<Tuple> ts, SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    List<Tuple> lhsNotUsed = notUsedIn(ts, lhs);
    List<Tuple> rhsNotUsed = notUsedIn(ts, rhs);
    if (lhsNotUsed.isEmpty() && rhsNotUsed.isEmpty())
      return;
    if (lhsNotUsed.isEmpty())
      lhsNotUsed.add(lhs.get(0));
    if (rhsNotUsed.isEmpty())
      rhsNotUsed.add(rhs.get(0));
    for (int i = 0; i < max(lhsNotUsed.size(), rhsNotUsed.size()); i++) {
      ts.add(connect(
          lhsNotUsed.get(i % lhsNotUsed.size()),
          rhsNotUsed.get(i % rhsNotUsed.size())
      ));
    }
  }

  private static List<Tuple> notUsedIn(List<Tuple> ts, SchemafulTupleSet lhs) {
    Set<Tuple> usedLhs = ts.stream()
        .map(each -> project(each, lhs.getAttributeNames()))
        .collect(Collectors.toSet());
    return lhs.stream()
        .filter(each -> !usedLhs.contains(each))
        .collect(toList());
  }

  private static Stream<List<String>> streamInvolvedFactorNames(int strength, String pi, List<String> processedFactors, List<String> rhsFactorNames) {
    return involvedFactorNames(strength, pi, processedFactors, rhsFactorNames).stream();
  }

  private static List<List<String>> involvedFactorNames(int strength, String pi, List<String> processedFactors, List<String> rhsFactorNames) {
    List<List<String>> ret = new LinkedList<>();
    for (int i = 1; i < strength; i++) {
      int j = strength - i - 1;
      new Combinator<>(rhsFactorNames, i).forEach(
          fromRhs -> new Combinator<>(processedFactors, j).forEach(
              fromProcessed -> ret.add(
                  new ArrayList<String>(strength) {
                    {
                      add(pi);
                      addAll(fromProcessed);
                      addAll(fromRhs);
                    }
                  }
              ))
      );
    }
    return ret;
  }

  private static FrameworkException noAvailableValueFor(String pi, Tuple tuple) {
    throw new FrameworkException(String.format("No covering tuple can be generated for: [%s,?| %s]", pi, tuple)) {
    };
  }

  static class Session {
    final         SchemafulTupleSet                                               lhs;
    final         SchemafulTupleSet                                               rhs;
    final private Function<Tuple, List<Tuple>>                                    coveredByLhs;
    final private Function<Tuple, List<Tuple>>                                    coveredByRhs;
    /**
     * in
     * 0: strength
     * 1: lhsTuple
     * 2: rhsTuple
     * out
     * subtuples(tuplets) whose length is equal to strength that connect lhsTuple
     * and rhsTuple
     */
    final private Function<Integer, Function<Tuple, Function<Tuple, Set<Tuple>>>> connectingSubtuplesOf;
    final private Function<Tuple, Function<Tuple, Tuple>>                         connect;
    private final Requirement                                                     requirement;

    Session(Requirement requirement, SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      this.requirement = requirement;
      this.lhs = lhs;
      this.rhs = rhs;
      this.coveredByLhs = memoize(
          tuple -> findCoveringTuplesIn(project(tuple, lhs.getAttributeNames()), lhs)
      );
      this.coveredByRhs = memoize(
          tuple -> findCoveringTuplesIn(project(tuple, rhs.getAttributeNames()), rhs)
      );
      this.connectingSubtuplesOf = memoize(
          strength -> memoize(
              (Function<Tuple, Function<Tuple, Set<Tuple>>>) lhsTuple -> memoize(
                  rhsTuple -> connectingSubtuplesOf(HashSet::new, lhsTuple, rhsTuple, strength)
              )
          )
      );
      this.connect = memoize(
          left -> memoize(
              right -> TupleUtils.connect(left, right)
          ));
    }

    private Tuple connect(Tuple tuple1, Tuple tuple2) {
      return connect.apply(tuple1).apply(tuple2);
    }

    private void findBestCombinationsFor(Tuple tupleToCover_, List<Tuple> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
      List<Tuple> tuplesToCover = Stream.concat(
          Stream.of(tupleToCover_),
          remainingTuplesToBeCovered.stream().filter(
              tuple -> tupleToCover_.keySet().stream().allMatch(tuple::containsKey) && !tupleToCover_.equals(tuple)
          )
      ).collect(toList());
      int[] last = new int[] { Integer.MAX_VALUE };
      tuplesToCover.forEach(
          each -> {
            int most = 0;
            Tuple bestLhs = null, bestRhs = null;
            outer:
            for (Tuple lhsTuple : this.coveredByLhs.apply(each)) {
              for (Tuple rhsTuple : this.coveredByRhs.apply(each)) {
                if (alreadyUsed.contains(connect(lhsTuple, rhsTuple)))
                  continue;
                Set<Tuple> connectingSubtuples = this.connectingSubtuplesOf.apply(requirement.strength()).apply(lhsTuple).apply(rhsTuple);
                int numCovered = sizeOfIntersection(
                    connectingSubtuples,
                    remainingTuplesToBeCovered.toSet()
                );
                if (numCovered > most) {
                  most = numCovered;
                  bestLhs = lhsTuple;
                  bestRhs = rhsTuple;
                  if (last[0] == most || most == remainingTuplesToBeCovered.size() || most == connectingSubtuples.size())
                    break outer;
                }
              }
            }
            last[0] = most;
            if (bestLhs != null && bestRhs != null) {
              alreadyUsed.add(connect(bestLhs, bestRhs));
              remainingTuplesToBeCovered.removeAll(
                  connectingSubtuplesOf
                      .apply(requirement.strength())
                      .apply(bestLhs)
                      .apply(bestRhs)
              );
            }
          }
      );
    }

    List<Tuple> hg(SchemafulTupleSet lhs, SchemafulTupleSet rhs, int strength, String pi, List<String> processedFactors, List<Tuple> ts, @SuppressWarnings("NonAsciiCharacters") TupleSet π) {
      class Util {
        List<List<String>>             involvedFactorNames         = involvedFactorNames(strength, pi, processedFactors, rhs.getAttributeNames());
        Supplier<Stream<List<String>>> involvedFactorNamesStreamer = involvedFactorNames::stream;

        private List<Object> valuesOfPiFor(Tuple tuple) {
          return lhs.index().find(projectLhs(tuple)).stream().map(each -> each.get(pi)).distinct().collect(toList());
        }

        private Tuple tupleWhosePiIs(Tuple tuple, Object o) {
          return Tuple.builder().putAll(tuple).put(pi, o).build();
        }

        private Tuple projectLhs(Tuple tuple) {
          return project(tuple, lhs.getAttributeNames());
        }

        private Tuple projectRhs(Tuple tuple) {
          return project(tuple, rhs.getAttributeNames());
        }

        private long count(Tuple t) {
          return involvedFactorNamesStreamer.get().map(
              factorNames -> project(t, factorNames)
          ).map(
              π::contains
          ).count(
          );
        }
      }
      Util util = new Util();
      List<Tuple> ret = new ArrayList<>(ts.size());

      for (Tuple each : ts) {
        if (each.keySet().size() == lhs.size() + rhs.size())
          continue;
        Tuple chosenTuple = util.tupleWhosePiIs(
            each,
            util.valuesOfPiFor(each).stream().max(
                (v, w) -> (int) (util.count(util.tupleWhosePiIs(each, w)) - util.count(util.tupleWhosePiIs(each, v)))
            ).orElseThrow(
                () -> noAvailableValueFor(pi, each)
            )
        );
        connectingSubtuplesOf
            .apply(strength)
            .apply(util.projectLhs(chosenTuple))
            .apply(util.projectRhs(chosenTuple))
            .forEach(
                π::remove
            );
        ret.add(chosenTuple);
      }
      return ret;
    }
  }

  public static void main(String... args) {
    System.out.println(Arrays.stream(new Integer[] { 1, 2, 3 }).sorted((o1, o2) -> o1 - o2).collect(toList()));
  }
}
