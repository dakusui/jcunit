package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.combinatoradix.Cartesianator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.subtuplesOf;
import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Joiner extends BinaryOperator<SchemafulTupleSet> {
  abstract class Base implements Joiner {
    @Override
    public SchemafulTupleSet apply(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      FrameworkException.checkCondition(Collections.disjoint(lhs.getAttributeNames(), rhs.getAttributeNames()));
      if (lhs.size() > rhs.size())
        return outerJoin(lhs, rhs);
      if (lhs.isEmpty())
        return SchemafulTupleSet.empty(new LinkedList<String>() {{
          addAll(lhs.getAttributeNames());
          addAll(rhs.getAttributeNames());
        }});
      return outerJoin(rhs, lhs);
    }

    protected abstract SchemafulTupleSet outerJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs);
  }

  class Standard extends Base {
    private final Requirement requirement;

    public Standard(Requirement requirement) {
      this.requirement = requireNonNull(requirement);
    }

    @Override
    protected SchemafulTupleSet outerJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
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
      return new SchemafulTupleSet.Builder(
          Stream.concat(
              lhs.getAttributeNames().stream(),
              rhs.getAttributeNames().stream()
          ).collect(toList()))
          .addAll(work)
          .build();
    }

    private Tuple connect(Tuple tuple1, Tuple tuple2) {
      return new Tuple.Builder().putAll(tuple1).putAll(tuple2).build();
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


