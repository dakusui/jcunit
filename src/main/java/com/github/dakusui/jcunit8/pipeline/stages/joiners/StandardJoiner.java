package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.connectingSubtuplesOf;
import static com.github.dakusui.jcunit.core.tuples.TupleUtils.project;
import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.sizeOfIntersection;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class StandardJoiner extends Joiner.Base {
  private static long        cur;
  private final  Requirement requirement;

  public StandardJoiner(Requirement requirement) {
    this.requirement = requireNonNull(requirement);
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    class Session {
      final private Function<Tuple, List<Tuple>>                                    coveredByLhs          = memoize(
          tuple -> findCoveringTuplesIn(project(tuple, lhs.getAttributeNames()), lhs)
      );
      final private Function<Tuple, List<Tuple>>                                    coveredByRhs          = memoize(
          tuple -> findCoveringTuplesIn(project(tuple, rhs.getAttributeNames()), rhs)
      );
      final private Function<Integer, Function<Tuple, Function<Tuple, Set<Tuple>>>> connectingSubtuplesOf =
          memoize(
              strength -> memoize(
                  (Function<Tuple, Function<Tuple, Set<Tuple>>>) lhsTuple -> memoize(
                      rhsTuple -> connectingSubtuplesOf(HashSet::new, lhsTuple, rhsTuple, strength)
                  )
              )
          );
      final private Function<Tuple, Function<Tuple, Tuple>>                         connect               = memoize(
          left -> memoize(
              right -> TupleUtils.connect(left, right)
          )
      );

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
                      remainingTuplesToBeCovered
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

      private Optional<Tuple> findBestTupleFor(Tuple tuple, List<Tuple> candidates, List<Tuple> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
        int most = 0;
        Tuple bestRhs = null;
        for (Tuple rhsTuple : candidates) {
          if (alreadyUsed.contains(connect(tuple, rhsTuple)))
            continue;
          Set<Tuple> connectingSubtuples = this.connectingSubtuplesOf.apply(requirement.strength()).apply(tuple).apply(rhsTuple);
          int numCovered = sizeOfIntersection(
              connectingSubtuples,
              remainingTuplesToBeCovered
          );
          if (numCovered > most) {
            most = numCovered;
            bestRhs = rhsTuple;
            if (most == remainingTuplesToBeCovered.size() || most == connectingSubtuples.size())
              break;
          }
        }
        return most == 0 ?
            Optional.empty() :
            Optional.of(bestRhs);
      }
    }
    log(String.format("phase-0:lhs[%s],rhs[%s],strength=%s", lhs.getAttributeNames().size(), rhs.getAttributeNames().size(), this.requirement.strength()));
    Session session = new Session();
    TupleSet remainingTuplesToBeCovered = computeTuplesToBeCovered(lhs, rhs, this.requirement.strength());
    List<Tuple> work = new LinkedList<>();
    ////
    // If there are tuples in lhs not used in work, they should be added to the
    // list. Otherwise t-way tuples covered by them will not be covered by the
    // final result. Same thing can be said in rhs.
    //
    // Modified HG (horizontal growth) procedure
    checkcond(lhs.size() >= rhs.size());
    log("phase-1:" + lhs.size());
    for (int i = 0; i < lhs.size(); i++) {
      Tuple lhsTuple = lhs.get(i);
      Tuple rhsTuple = i < rhs.size() ?
          rhs.get(i) :
          session.findBestTupleFor(lhsTuple, rhs, work, remainingTuplesToBeCovered).orElse(
              rhs.get(i % rhs.size())
          );
      Tuple tuple = session.connect(lhsTuple, rhsTuple);
      work.add(tuple);
      remainingTuplesToBeCovered.removeAll(session.connectingSubtuplesOf.apply(this.requirement.strength()).apply(lhsTuple).apply(rhsTuple));
    }
    log("phase-2:remainingTuples=" + remainingTuplesToBeCovered.size());
    ////
    // Modified VG (vertical growth) procedure
    while (!remainingTuplesToBeCovered.isEmpty()) {
      session.findBestCombinationsFor(remainingTuplesToBeCovered.stream().findFirst().orElseThrow(
          IllegalStateException::new
          ),
          work,
          remainingTuplesToBeCovered);
    }
    log("phase-3");
    return new SchemafulTupleSet.Builder(
        Stream.concat(
            lhs.getAttributeNames().stream(),
            rhs.getAttributeNames().stream()
        ).collect(toList()))
        .addAll(work)
        .build();
  }

  static void log(String label, Object... args) {
    long now = System.currentTimeMillis();
    long time = cur == 0 ? 0 : now - cur;
    if (isDebugEnabled())
      System.out.println(String.format(label, args) + ":" + time);
    cur = now;
  }

  public static boolean isDebugEnabled() {
    return "yes".equals(System.getProperty("debug"));
  }

  static List<Tuple> findCoveringTuplesIn(Tuple aTuple, SchemafulTupleSet tuples) {
    Tuple inConcern = project(aTuple, tuples.getAttributeNames());
    return tuples.stream(
    ).filter(
        inConcern::isSubtupleOf
    ).collect(
        toList()
    );
  }

  private static TupleSet computeTuplesToBeCovered(SchemafulTupleSet lhs, SchemafulTupleSet rhs, int strength) {
    TupleSet.Builder builder = new TupleSet.Builder();
    for (int i = 1; i < strength; i++) {
      int j = strength - i;
      if (j > lhs.getAttributeNames().size())
        continue;
      TupleSet lhsTupleSet = lhs.subtuplesOf(j);
      if (i > rhs.getAttributeNames().size())
        break;
      TupleSet rhsTupleSet = rhs.subtuplesOf(i);
      builder.addAll(lhsTupleSet.cartesianProduct(rhsTupleSet));
    }
    return builder.build();
  }

}
