package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.combinatoradix.Cartesianator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.*;
import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.sizeOfIntersection;
import static java.util.Arrays.asList;
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
        System.out.printf("%s:%d%n", label, System.currentTimeMillis() - before);
        this.before = System.currentTimeMillis();
      }
    }

    protected SchemafulTupleSet __doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      StopWatch stopWatch = new StopWatch();
      TupleSet allTuplesToBeCovered = computeTuplesToBeCovered(lhs, rhs, this.requirement.strength());
      stopWatch.print("computeTuplesToBeCovered");
      List<Tuple> work = new LinkedList<>();
      ////
      // If there are tuples in lhs not used in work, they should be added to the
      // list. Otherwise t-way tuples covered by them will not be covered by the
      // final result. Same thing can be said in rhs.
      checkcond(lhs.size() >= rhs.size());
      for (int i = 0; i < lhs.size(); i++) {
        Tuple tuple = connect(lhs.get(i), rhs.get(i % rhs.size()));
        work.add(tuple);
        allTuplesToBeCovered.removeAll(subtuplesOf(tuple, this.requirement.strength()));
      }
      stopWatch.print("HG");
      ////
      // Modified VG (vertical growth) procedure
      List<Tuple> allFullTuples = new LinkedList<>(
          new TupleCartesianator(asList(
              lhs, rhs
          )).stream(
          ).filter(
              tuple -> !work.contains(tuple)
          ).collect(
              toList()
          )
      );
      stopWatch.print("VG-1");
      while (!allTuplesToBeCovered.isEmpty()) {
        Tuple tuple = chooseBestTupleFrom(SchemafulTupleSet.fromTuples(allFullTuples), allTuplesToBeCovered, lhs.width(), rhs.width());
        if (!allTuplesToBeCovered.removeAll(subtuplesOf(tuple, this.requirement.strength())))
          break;
        work.add(tuple);
      }
      stopWatch.print("VG-2");
      return new SchemafulTupleSet.Builder(
          Stream.concat(
              lhs.getAttributeNames().stream(),
              rhs.getAttributeNames().stream()
          ).collect(toList()))
          .addAll(work)
          .build();
    }

    @Override
    protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      Function<Tuple, List<Tuple>> coveredByLhs = memoize(
          tuple -> findCoveringTuplesIn(project(tuple, lhs.getAttributeNames()), lhs)
      );
      Function<Tuple, List<Tuple>> coveredByRhs = memoize(
          tuple -> findCoveringTuplesIn(project(tuple, rhs.getAttributeNames()), rhs)
      );

      TupleSet remainingTuplesToBeCovered = computeTuplesToBeCovered(lhs, rhs, this.requirement.strength());
      List<Tuple> work = new LinkedList<>();
      ////
      // If there are tuples in lhs not used in work, they should be added to the
      // list. Otherwise t-way tuples covered by them will not be covered by the
      // final result. Same thing can be said in rhs.
      checkcond(lhs.size() >= rhs.size());
      StopWatch stopWatch = new StopWatch();
      for (int i = 0; i < lhs.size(); i++) {
        Tuple lhsTuple = lhs.get(i);
        Tuple rhsTuple = rhs.get(i % rhs.size());
        Tuple tuple = connect(lhsTuple, rhsTuple);
        work.add(tuple);
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(lhsTuple, rhsTuple, this.requirement.strength()));
      }
      stopWatch.print("HG");
      while (!remainingTuplesToBeCovered.isEmpty()) {
        Tuple target = remainingTuplesToBeCovered.stream().findFirst().orElseThrow(IllegalStateException::new);
        int most = 0;
        Tuple bestLhs = null, bestRhs = null;
        for (Tuple lhsTuple : coveredByLhs.apply(target)) {
          for (Tuple rhsTuple : coveredByRhs.apply(target)) {
            if (work.contains(connect(lhsTuple, rhsTuple)))
              continue;
            int numCovered = sizeOfIntersection(
                connectingSubtuplesOf(lhsTuple, rhsTuple, this.requirement.strength()),
                remainingTuplesToBeCovered);
            if (numCovered > most)
              most = numCovered;
            bestLhs = lhsTuple;
            bestRhs = rhsTuple;
          }
        }
        checkcond(most > 0);
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(bestLhs, bestRhs, this.requirement.strength()));
        work.add(connect(bestLhs, bestRhs));
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

    protected SchemafulTupleSet _doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      StopWatch stopWatch = new StopWatch();
      TupleSet allTuplesToBeCovered = computeTuplesToBeCovered(lhs, rhs, this.requirement.strength());
      stopWatch.print("computeTuplesToBeCovered");
      List<Tuple> work = new LinkedList<>();
      {
        ////
        // Modified HG (horizontal growth) procedure
        for (Tuple each : lhs) {
          Tuple tuple = connect(chooseBestTupleFrom(each, rhs, allTuplesToBeCovered), each);
          if (!allTuplesToBeCovered.removeAll(subtuplesOf(tuple, this.requirement.strength())))
            break;
          work.add(tuple);
          if (allTuplesToBeCovered.isEmpty())
            break;
        }
      }
      stopWatch.print("HG");
      ////
      // Modified VG (vertical growth) procedure
      List<Tuple> allFullTuples = new LinkedList<>(
          new TupleCartesianator(asList(
              lhs, rhs
          )).stream(
          ).filter(
              tuple -> !work.contains(tuple)
          ).collect(
              toList()
          )
      );
      while (!allTuplesToBeCovered.isEmpty()) {
        Tuple tuple = chooseBestTupleFrom(
            SchemafulTupleSet.fromTuples(allFullTuples),
            allTuplesToBeCovered,
            lhs.getAttributeNames().size(),
            rhs.getAttributeNames().size()
        );
        if (!allTuplesToBeCovered.removeAll(subtuplesOf(tuple, this.requirement.strength())))
          break;
        work.add(tuple);
      }
      stopWatch.print("VG");
      ////
      // If there are tuples in lhs not used in work, they should be added to the
      // list. Otherwise t-way tuples covered by them will not be covered by the
      // final result. Same thing can be said in rhs.
      lhs.stream().filter(
          each -> !isUsed(each, work)
      ).forEach(
          each -> work.add(connect(each, rhs.get(lhs.indexOf(each) % rhs.size())))
      );
      rhs.stream().filter(
          each -> !isUsed(each, work)
      ).forEach(
          each -> work.add(connect(lhs.get(rhs.indexOf(each)), each))
      );
      stopWatch.print("FINISH");
      return new SchemafulTupleSet.Builder(
          Stream.concat(
              lhs.getAttributeNames().stream(),
              rhs.getAttributeNames().stream()
          ).collect(toList()))
          .addAll(work)
          .build();
    }

    private boolean isUsed(Tuple tuple, List<Tuple> work) {
      return work.stream().anyMatch(tuple::isSubtupleOf);
    }

    private Tuple connect(Tuple tuple1, Tuple tuple2) {
      return new Tuple.Builder().putAll(tuple1).putAll(tuple2).build();
    }

    private Tuple chooseBestTupleFrom(SchemafulTupleSet fromTupleSuite, TupleSet allTuplesToBeCovered, int lhsSize, int rhsSize) {
      Function<Tuple, Long> counter = new Function<Tuple, Long>() {
        @Override
        public Long apply(Tuple value) {
          return coveredBy(value, allTuplesToBeCovered);
        }
      };
      List<Tuple> coversNothing = new LinkedList<>();
      try {
        return fromTupleSuite.stream(
        ).filter((Tuple value) -> {
          long count = counter.apply(value);
          return count >= Math.min(allTuplesToBeCovered.size(), lhsSize * rhsSize);
        }).findFirst(
        ).orElseGet(() -> fromTupleSuite.stream(
        ).max(Comparator.comparingLong(
            (Tuple value) -> {
              long num = coveredBy(value, allTuplesToBeCovered);
              if (num == 0)
                coversNothing.add(value);
              return num;
            })
        ).orElseThrow(FrameworkException::unexpectedByDesign));
      } finally {
        //        fromTupleSuite.removeAll(coversNothing);
      }
    }

    private Tuple chooseBestTupleFrom(Tuple fromLhs, SchemafulTupleSet rhs, TupleSet allTuplesToBeCovered) {
      return chooseBestTupleFrom(
          SchemafulTupleSet.fromTuples(
              rhs.stream()
                  .map(tuple -> connect(tuple, fromLhs))
                  .collect(toList())
          ),
          allTuplesToBeCovered,
          fromLhs.size(),
          rhs.getAttributeNames().size()
      );
    }

    private long coveredBy(Tuple tuple, TupleSet tuples) {
      return subtuplesOf(tuple, this.requirement.strength()).stream()
          .filter(tuples::contains)
          .count();
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

    private static class TupleCartesianator extends AbstractList<Tuple> {
      private final Cartesianator<Tuple> inner;

      TupleCartesianator(List<? extends List<? extends Tuple>> sets) {
        this.inner = new Cartesianator<Tuple>(sets) {
        };
      }

      @Override
      public Tuple get(int index) {
        Tuple.Builder builder = new Tuple.Builder();
        this.inner.get(index).forEach(builder::putAll);
        return builder.build();
      }

      @Override
      public int size() {
        return (int) inner.size();
      }
    }
  }
}


