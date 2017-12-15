package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.connectingSubtuplesOf;
import static com.github.dakusui.jcunit.core.tuples.TupleUtils.project;
import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.sizeOfIntersection;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Joiner extends BinaryOperator<SchemafulTupleSet> {
  abstract class Base implements Joiner {
    @Override
    public SchemafulTupleSet apply(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      FrameworkException.checkCondition(Collections.disjoint(lhs.getAttributeNames(), rhs.getAttributeNames()));
      if (lhs.isEmpty() || rhs.isEmpty())
        return emptyTupleSet(lhs, rhs);
      if (lhs.size() > rhs.size())
        return doJoin(lhs, rhs);
      return doJoin(rhs, lhs);
    }

    private SchemafulTupleSet emptyTupleSet(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      return SchemafulTupleSet.empty(new LinkedList<String>() {{
        addAll(lhs.getAttributeNames());
        addAll(rhs.getAttributeNames());
      }});
    }

    protected abstract SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs);
  }

  class Standard extends Base {
    private static long        cur;
    private final  Requirement requirement;

    public Standard(Requirement requirement) {
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
                right -> _connect(left, right)
            )
        );

        private Tuple connect(Tuple tuple1, Tuple tuple2) {
          return connect.apply(tuple1).apply(tuple2);
        }

        private void findBestCombinationsFor2(Tuple tupleToCover_, List<Tuple> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          log("phase-2.0:" + alreadyUsed.size() + "/" + tupleToCover_);
          List<Tuple> tuplesToCover = Stream.concat(
              Stream.of(tupleToCover_),
              remainingTuplesToBeCovered.stream().filter(
                  tuple -> tupleToCover_.keySet().stream().allMatch(tuple::containsKey) && !tupleToCover_.equals(tuple)
              )
          ).collect(toList());
          List<String> keys = new ArrayList<>(tupleToCover_.keySet());

          log("phase-2.1:" + tuplesToCover.size() + ": lhs=" + lhs.size() + ": rhs=" + rhs.size());
          Map<Tuple, Tuple> bestFor = new HashMap<>();
          Map<Tuple, Integer> numCoveredByBestFor = new HashMap<>();
          outer:
          for (Tuple lhsTuple : lhs) {
            for (Tuple rhsTuple : rhs) {
              if (remainingTuplesToBeCovered.isEmpty())
                break outer;
              Tuple connected = connect(lhsTuple, rhsTuple);
              if (alreadyUsed.contains(connected))
                continue;
              Set<Tuple> connectingSubtuples = connectingSubtuplesOf.apply(requirement.strength()).apply(lhsTuple).apply(rhsTuple);
              //Set<Tuple> connectingSubtuples = TupleUtils.connectingSubtuplesOf(HashSet::new, lhsTuple,rhsTuple, requirement.strength());
              int numCovered = sizeOfIntersection(
                  connectingSubtuples,
                  remainingTuplesToBeCovered
              );
              if (numCovered == 0)
                continue;
              Tuple each = project(connected, keys);
              if (!numCoveredByBestFor.containsKey(each) || numCovered > numCoveredByBestFor.get(each)) {
                bestFor.put(each, connected);
                numCoveredByBestFor.put(each, numCovered);
              }
            }
          }
          log("phase-2.1.2:" + alreadyUsed.size() + ":" + remainingTuplesToBeCovered.size());

          for (Tuple each : tuplesToCover) {
            if (numCoveredByBestFor.containsKey(each)) {
              Tuple connected = bestFor.get(each);
              alreadyUsed.add(connected);
              remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf.apply(requirement.strength()).apply(project(connected, lhs.getAttributeNames())).apply(project(connected, rhs.getAttributeNames())));
            }
          }
          log("phase-2.1.3:" + alreadyUsed.size() + ":" + remainingTuplesToBeCovered.size());
        }

        private void findBestCombinationsFor(Tuple tupleToCover_, List<Tuple> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          log("phase-2.0:" + alreadyUsed.size() + "/" + tupleToCover_);
          List<Tuple> tuplesToCover = Stream.concat(
              Stream.of(tupleToCover_),
              remainingTuplesToBeCovered.stream().filter(
                  tuple -> tupleToCover_.keySet().stream().allMatch(tuple::containsKey) && !tupleToCover_.equals(tuple)
              )
          ).collect(toList());
          log("phase-2.1:" + tuplesToCover.size() + ":" + tuplesToCover);
          int[] last = new int[] { Integer.MAX_VALUE };
          tuplesToCover.forEach(
              each -> {
                log("phase-2.2.x:" + remainingTuplesToBeCovered.size() + ":" + alreadyUsed.size());
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
                      if (last[0] == most)
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

        private Optional<Tuple> findBestCombinationFor_(Tuple tupleToCover, List<Tuple> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          int most = 0;
          Tuple bestLhs = null, bestRhs = null;
          for (Tuple lhsTuple : this.coveredByLhs.apply(tupleToCover)) {
            for (Tuple rhsTuple : this.coveredByRhs.apply(tupleToCover)) {
              if (alreadyUsed.contains(connect(lhsTuple, rhsTuple)))
                continue;
              Set<Tuple> connectingSubtuples = this.connectingSubtuplesOf.apply(requirement.strength()).apply(lhsTuple).apply(rhsTuple);
              int numCovered = sizeOfIntersection(
                  connectingSubtuples,
                  remainingTuplesToBeCovered);
              if (numCovered > most) {
                most = numCovered;
                bestLhs = lhsTuple;
                bestRhs = rhsTuple;
              }
            }
          }
          return most == 0 ?
              Optional.empty() :
              Optional.of(connect(bestLhs, bestRhs));
        }

        private Optional<Tuple> findBestRhsFor(Tuple lhsTuple, List<Tuple> rhs, List<Tuple> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          int most = 0;
          Tuple bestRhs = null;
          for (Tuple rhsTuple : rhs) {
            if (alreadyUsed.contains(connect(lhsTuple, rhsTuple)))
              continue;
            int numCovered = sizeOfIntersection(
                this.connectingSubtuplesOf.apply(requirement.strength()).apply(lhsTuple).apply(rhsTuple),
                remainingTuplesToBeCovered
            );
            if (numCovered > most) {
              most = numCovered;
              bestRhs = rhsTuple;
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
            session.findBestRhsFor(lhsTuple, rhs, work, remainingTuplesToBeCovered).orElse(
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
        // TODO: This is too much
        /*
        Tuple bestTuple = session.findBestCombinationFor(
            remainingTuplesToBeCovered.stream().findFirst().orElseThrow(
                IllegalStateException::new
            ),
            work,
            remainingTuplesToBeCovered
        ).orElseThrow(
            IllegalStateException::new
        );

        */

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

    private static void log(String label) {
      long now = System.currentTimeMillis();
      long time = cur == 0 ? 0 : now - cur;
      System.out.println(label + ":" + time);
      cur = now;
    }

    private List<Tuple> findCoveringTuplesIn(Tuple aTuple, SchemafulTupleSet tuples) {
      Tuple inConcern = project(aTuple, tuples.getAttributeNames());
      return tuples.stream(
      ).filter(
          inConcern::isSubtupleOf
      ).collect(
          toList()
      );
    }

    private static Tuple _connect(Tuple tuple1, Tuple tuple2) {
      return Tuple.builder().putAll(tuple1).putAll(tuple2).build();
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
}


