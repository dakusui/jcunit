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
      if (lhs.size() > rhs.size())
        return doJoin(lhs, rhs);
      if (lhs.isEmpty())
        return SchemafulTupleSet.empty(new LinkedList<String>() {{
          addAll(lhs.getAttributeNames());
          addAll(rhs.getAttributeNames());
        }});
      return doJoin(rhs, lhs);
    }

    protected abstract SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs);
  }

  class Standard extends Base {
    private final Requirement requirement;

    public Standard(Requirement requirement) {
      this.requirement = requireNonNull(requirement);
    }

    class StopWatch {
      long before;

      StopWatch() {
        this.before = System.currentTimeMillis();
      }

      void print(String label) {
        //System.out.printf("%s:%d%n", label, System.currentTimeMillis() - before);
        this.before = System.currentTimeMillis();
      }
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
                        rhsTuple -> connectingSubtuplesOf(lhsTuple, rhsTuple, strength)
                    )
                )
            );

        private Optional<Tuple> findBestCombinationFor(Tuple tupleToCover, List<Tuple> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          int most = 0;
          Tuple bestLhs = null, bestRhs = null;
          for (Tuple lhsTuple : this.coveredByLhs.apply(tupleToCover)) {
            for (Tuple rhsTuple : this.coveredByRhs.apply(tupleToCover)) {
              if (alreadyUsed.contains(connect(lhsTuple, rhsTuple)))
                continue;
              int numCovered = sizeOfIntersection(
                  this.connectingSubtuplesOf.apply(requirement.strength()).apply(lhsTuple).apply(rhsTuple),
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
      StopWatch stopWatch = new StopWatch();
      for (int i = 0; i < lhs.size(); i++) {
        Tuple lhsTuple = lhs.get(i);
        Tuple rhsTuple = i < rhs.size() ?
            rhs.get(i) :
            session.findBestRhsFor(lhsTuple, rhs, work, remainingTuplesToBeCovered).orElse(
                rhs.get(i % rhs.size())
            );
        Tuple tuple = connect(lhsTuple, rhsTuple);
        work.add(tuple);
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(lhsTuple, rhsTuple, this.requirement.strength()));
      }
      stopWatch.print("HG");
      ////
      // Modified VG (vertical growth) procedure
      while (!remainingTuplesToBeCovered.isEmpty()) {
        Tuple bestTuple = session.findBestCombinationFor(
            remainingTuplesToBeCovered.stream().findFirst().orElseThrow(
                IllegalStateException::new
            ),
            work,
            remainingTuplesToBeCovered
        ).orElseThrow(
            IllegalStateException::new
        );

        work.add(bestTuple);
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(
            project(bestTuple, lhs.getAttributeNames()),
            project(bestTuple, rhs.getAttributeNames()),
            requirement.strength()
        ));
      }
      stopWatch.print("VG");
      return new SchemafulTupleSet.Builder(
          Stream.concat(
              lhs.getAttributeNames().stream(),
              rhs.getAttributeNames().stream()
          ).collect(toList()))
          .addAll(work)
          .build();
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

    private Tuple connect(Tuple tuple1, Tuple tuple2) {
      return new Tuple.Builder().putAll(tuple1).putAll(tuple2).build();
    }

    private static TupleSet computeTuplesToBeCovered(SchemafulTupleSet lhs, SchemafulTupleSet rhs, int strength) {
      TupleSet.Builder builder = new TupleSet.Builder();
      for (int i = 1; i < strength; i++) {
        TupleSet lhsTupleSet = lhs.subtuplesOf(strength - i);
        TupleSet rhsTupleSet = rhs.subtuplesOf(i);
        builder.addAll(lhsTupleSet.cartesianProduct(rhsTupleSet));
      }
      return builder.build();
    }

  }
}


