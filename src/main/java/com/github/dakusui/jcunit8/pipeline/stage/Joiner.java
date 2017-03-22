package com.github.dakusui.jcunit8.pipeline.stage;

import com.github.dakusui.combinatoradix.Cartesianator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Requirement;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.testsuite.TupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSuite;

import java.util.*;
import java.util.function.BinaryOperator;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.subtuplesOf;
import static com.github.dakusui.jcunit8.core.Utils.project;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Joiner extends BinaryOperator<TupleSuite> {
  abstract class Base implements Joiner {
    @Override
    public TupleSuite apply(TupleSuite lhs, TupleSuite rhs) {
      if (Collections.disjoint(lhs.getAttributeNames(), rhs.getAttributeNames())) {
        return outerJoin(lhs, rhs);
      }
      return innerJoin(overlap(lhs.getAttributeNames(), rhs.getAttributeNames()), lhs, rhs);
    }

    protected abstract TupleSuite outerJoin(TupleSuite lhs, TupleSuite rhs);

    protected abstract TupleSuite innerJoin(List<String> sharedKeys, TupleSuite lhs, TupleSuite rhs);

    private static List<String> overlap(List<String> lhs, List<String> rhs) {
      return lhs.stream().filter(rhs::contains).collect(toList());
    }
  }

  class Standard extends Base {
    private final Requirement requirement;

    public Standard(Requirement requirement) {
      this.requirement = requireNonNull(requirement);
    }

    @Override
    protected TupleSuite outerJoin(TupleSuite lhs, TupleSuite rhs) {
      TupleSet allTuplesToBeCovered = computeTuplesToBeCovered(lhs, rhs, this.requirement.strength());
      List<Tuple> work = new LinkedList<>();
      {
        ////
        // Modified HG (horizontal growth) procedure
        for (Tuple each : lhs) {
          Tuple tuple = connect(chooseBestTupleFrom(rhs, each, allTuplesToBeCovered), each);
          if (!allTuplesToBeCovered.removeAll(subtuplesOf(tuple, this.requirement.strength())))
            break;
          work.add(tuple);
          if (allTuplesToBeCovered.isEmpty())
            break;
        }
      }
      ////
      // Modified VG (vertical growth) procedure
      while (!allTuplesToBeCovered.isEmpty()) {
        Tuple tuple = chooseBestTupleFrom(new TupleCartesianator(asList(lhs, rhs)), allTuplesToBeCovered);
        if (!allTuplesToBeCovered.removeAll(subtuplesOf(tuple, this.requirement.strength())))
          break;
        work.add(tuple);
      }
      return TupleSuite.fromTuples(work);
    }

    @Override
    protected TupleSuite innerJoin(List<String> sharedKeys, TupleSuite lhs, TupleSuite rhs) {
      List<Tuple> work = new LinkedList<>();
      TupleSet allTuplesToBeCovered = computeTuplesToBeCovered(
          lhs,
          rhs.project(keysNotShared(sharedKeys, rhs)),
          requirement.strength()
      );
      lhs.forEach(
          (Tuple outer) -> rhs.stream()
              .filter((Tuple inner) -> Objects.equals(project(sharedKeys, outer), project(sharedKeys, inner)))
              .map((Tuple inner) -> connect(outer, inner))
              .filter((Tuple connected) -> subtuplesOf(connected, requirement.strength()).stream().anyMatch(allTuplesToBeCovered::contains))
              .forEach((Tuple connected) -> {
                allTuplesToBeCovered.removeAll(subtuplesOf(connected, requirement.strength()));
                work.add(connected);
              })
      );
      return TupleSuite.fromTuples(work);
    }

    private Tuple connect(Tuple tuple1, Tuple tuple2) {
      return new Tuple.Builder().putAll(tuple1).putAll(tuple2).build();
    }

    private List<String> keysNotShared(List<String> sharedKeys, TupleSuite tupleSuite) {
      return tupleSuite.getAttributeNames().stream().filter(s -> !sharedKeys.contains(s)).collect(toList());
    }

    private Tuple chooseBestTupleFrom(List<Tuple> fromTupleSuite, TupleSet allTuplesToBeCovered) {
      return fromTupleSuite.stream()
          .max(Comparator.comparingLong((Tuple value) -> coveredBy(value, allTuplesToBeCovered)))
          .orElseThrow(FrameworkException::unexpectedByDesign);
    }

    private Tuple chooseBestTupleFrom(List<Tuple> fromTupleSuite, Tuple forTuple, TupleSet allTuplesToBeCovered) {
      return chooseBestTupleFrom(
          fromTupleSuite.stream()
              .map(tuple -> connect(tuple, forTuple))
              .collect(toList()),
          allTuplesToBeCovered
      );
    }

    private long coveredBy(Tuple tuple, TupleSet tuples) {
      return subtuplesOf(tuple, this.requirement.strength()).stream()
          .filter(tuples::contains)
          .count();
    }

    private static TupleSet computeTuplesToBeCovered(TupleSuite lhs, TupleSuite rhs, int strength) {
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
        for (Tuple each : this.inner.get(index)) {
          builder.putAll(each);
        }
        return builder.build();
      }

      @Override
      public int size() {
        return (int) inner.size();
      }
    }
  }
}