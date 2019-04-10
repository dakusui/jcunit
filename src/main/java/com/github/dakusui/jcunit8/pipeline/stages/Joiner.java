package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.StreamableCombinator;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.*;
import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.sizeOfIntersection;
import static com.github.dakusui.jcunit8.pipeline.stages.Joiner.JoinerUtils.connect;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

public interface Joiner extends BinaryOperator<SchemafulTupleSet> {
  abstract class Base implements Joiner {
    private final Requirement requirement;

    protected Base(Requirement requirement) {
      this.requirement = requirement;
    }

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

    protected Requirement requirement() {
      return this.requirement;
    }

    protected abstract SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs);
  }

  class Standard extends Base {

    public Standard(Requirement requirement) {
      super(requirement);
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
                    lhsTuple -> memoize(
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
                  this.connectingSubtuplesOf.apply(requirement().strength()).apply(lhsTuple).apply(rhsTuple),
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
                this.connectingSubtuplesOf.apply(requirement().strength()).apply(lhsTuple).apply(rhsTuple),
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

      TupleSet remainingTuplesToBeCovered = computeTuplesToBeCovered(lhs, rhs, this.requirement().strength());
      List<Tuple> work = new LinkedList<>();
      ////
      // If there are tuples in lhs not used in work, they should be added to the
      // list. Otherwise t-way tuples covered by them will not be covered by the
      // final result. Same thing can be said in rhs.
      //
      // Modified HG (horizontal growth) procedure
      checkcond(lhs.size() >= rhs.size());
      for (int i = 0; i < lhs.size(); i++) {
        Tuple lhsTuple = lhs.get(i);
        Tuple rhsTuple = i < rhs.size() ?
            rhs.get(i) :
            session.findBestRhsFor(lhsTuple, rhs, work, remainingTuplesToBeCovered).orElse(
                rhs.get(i % rhs.size())
            );
        Tuple tuple = connect(lhsTuple, rhsTuple);
        work.add(tuple);
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(lhsTuple, rhsTuple, this.requirement().strength()));
      }
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
            requirement().strength()
        ));
      }
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

  class WeakenProduct extends Base {

    public WeakenProduct(Requirement requirement) {
      super(requirement);
    }

    @Override
    protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      if (rhs.isEmpty() || lhs.isEmpty())
        return lhs;

      SchemafulTupleSet.Builder b = new SchemafulTupleSet.Builder(new ArrayList<String>() {{
        addAll(lhs.getAttributeNames());
        addAll(rhs.getAttributeNames());
      }});
      Set<Tuple> leftoverWorkForLhs = new LinkedHashSet<>(lhs.size());
      Set<Tuple> leftoverWorkForRhs = new LinkedHashSet<>(lhs.size());
      leftoverWorkForLhs.addAll(lhs);
      leftoverWorkForRhs.addAll(rhs);
      LinkedHashSet<Tuple> work = new LinkedHashSet<>();
      {
        Function<Function<SchemafulTupleSet, Function<Integer, Set<Tuple>>>, Function<SchemafulTupleSet, Function<Integer, SchemafulTupleSet>>> weakener
            = memoize(tupletsFinder -> memoize(in -> memoize(strength -> JoinerUtils.weakenTo(in, strength, tupletsFinder))));
        Function<SchemafulTupleSet, Function<Integer, Set<Tuple>>> tupletsFinder
            = memoize(in -> memoize(strength -> JoinerUtils.tupletsCoveredBy(in, strength)));
        for (int i = 1; i < requirement().strength(); i++) {
          SchemafulTupleSet weakenedLhs = weakener.apply(tupletsFinder).apply(lhs).apply(i);
          SchemafulTupleSet weakenedRhs = weakener.apply(tupletsFinder).apply(rhs).apply(requirement().strength() - i);
          addConnectedTuples(work, weakenedLhs, weakenedRhs);
          leftoverWorkForLhs.removeAll(weakenedLhs);
          leftoverWorkForRhs.removeAll(weakenedRhs);
        }
      }

      ensureLeftoversArePresent(work,
          leftoverWorkForLhs, leftoverWorkForRhs,
          lhs.get(0), rhs.get(0));
      b.addAll(new ArrayList<>(work));
      return b.build();
    }

    void addConnectedTuples(LinkedHashSet<Tuple> work, SchemafulTupleSet weakenedLhs, SchemafulTupleSet weakenedRhs) {
      work.addAll(
          JoinerUtils.cartesianProduct(
              weakenedLhs,
              weakenedRhs));
    }

    private void ensureLeftoversArePresent(
        LinkedHashSet<Tuple> b,
        Set<Tuple> leftoverWorkForLhs, Set<Tuple> leftoverWorkForRhs,
        Tuple firstTupleInLhs,
        Tuple firstTupleInRhs) {
      if (leftoverWorkForLhs.isEmpty() && leftoverWorkForRhs.isEmpty()) {
        return;
      }
      if (leftoverWorkForLhs.size() > leftoverWorkForRhs.size())
        ensureLeftoversArePresent_(
            b,
            leftoverWorkForLhs, leftoverWorkForRhs, firstTupleInRhs);
      else
        ensureLeftoversArePresent_(
            b,
            leftoverWorkForRhs, leftoverWorkForLhs, firstTupleInLhs);
    }

    private void ensureLeftoversArePresent_(
        LinkedHashSet<Tuple> b,
        Set<Tuple> biggerLeftover, Set<Tuple> nonBiggerLeftover,
        Tuple firstTupleInNonBigger) {
      int max = max(biggerLeftover.size(), nonBiggerLeftover.size());
      int min = min(biggerLeftover.size(), nonBiggerLeftover.size());
      List<Tuple> leftOverFromBigger = new ArrayList<Tuple>() {{
        addAll(biggerLeftover);
      }};
      List<Tuple> leftOverFromNonBigger = new ArrayList<Tuple>() {{
        addAll(nonBiggerLeftover);
      }};
      for (int i = 0; i < max; i++) {
        if (i < min)
          b.add(connect(leftOverFromBigger.get(i), leftOverFromNonBigger.get(i)));
        else
          b.add(connect(leftOverFromBigger.get(i), firstTupleInNonBigger));
      }
    }
  }

  public static class WeakenProduct2 extends WeakenProduct {
    private final Function<Integer, Function<List<String>, Function<List<String>, Set<List<String>>>>> composeColumnSelections;
    Set<Tuple> coveredCrossingTuplets = new HashSet<>();

    public WeakenProduct2(Requirement requirement) {
      super(requirement);
      this.composeColumnSelections = memoize((Integer i) -> memoize((List<String> l) -> memoize((List<String> r) -> composeColumnSelections(i, l, r))));
    }

    void addConnectedTuples(LinkedHashSet<Tuple> work, SchemafulTupleSet weakenedLhs, SchemafulTupleSet weakenedRhs) {
      for (Tuple eachFromLhs : weakenedLhs) {
        for (Tuple eachFromRhs : weakenedRhs) {
          int numTupletsBeforeAdding = coveredCrossingTuplets.size();
          coveredCrossingTuplets.addAll(crossingTuplets(eachFromLhs, eachFromRhs, requirement().strength()));
          if (coveredCrossingTuplets.size() > numTupletsBeforeAdding)
            work.add(connect(eachFromLhs, eachFromRhs));
        }
      }
    }

    private Collection<? extends Tuple> crossingTuplets(Tuple eachFromLhs, Tuple eachFromRhs, int strength) {
      Tuple connected = connect(eachFromLhs, eachFromRhs);
      return composeColumnSelections
          .apply(strength)
          .apply(new ArrayList<>(eachFromLhs.keySet()))
          .apply(new ArrayList<>(eachFromRhs.keySet()))
          .stream()
          .map(each -> project(connected, each))
          .collect(Collectors.toList());
    }

    private Set<List<String>> composeColumnSelections(int strength, List<String> lhsColumns, List<String> rhsColumns) {
      Set<List<String>> columnSelections = new HashSet<>();
      for (int i = 1; i <= strength - 1; i++) {
        StreamableCombinator<String> lhs = new StreamableCombinator<>(lhsColumns, i);

        int finalI = i;
        lhs.stream()
            .flatMap((Function<List<String>, Stream<List<String>>>) fromLhs ->
                new StreamableCombinator<>(rhsColumns, finalI)
                    .stream()
                    .map(fromRhs -> new LinkedList<String>() {{
                      addAll(fromLhs);
                      addAll(fromRhs);
                    }}))
            .forEach(columnSelections::add);
      }
      return columnSelections;
    }
  }

  enum JoinerUtils {
    ;

    static Tuple connect(Tuple tuple1, Tuple tuple2) {
      return new Tuple.Builder().putAll(tuple1).putAll(tuple2).build();
    }

    private static SchemafulTupleSet weakenTo(SchemafulTupleSet in, int strength, Function<SchemafulTupleSet, Function<Integer, Set<Tuple>>> coveredTupletsFinder) {
      SchemafulTupleSet.Builder b = new SchemafulTupleSet.Builder(in.getAttributeNames());
      Set<Tuple> tupletsToBeCovered = coveredTupletsFinder.apply(in).apply(strength);
      for (Tuple each : in) {
        int before = tupletsToBeCovered.size();
        tupletsToBeCovered.removeAll(subtuplesOf(each, strength));
        if (tupletsToBeCovered.size() < before)
          b.add(each);
        if (tupletsToBeCovered.isEmpty())
          break;
      }
      return b.build();
    }

    private static Set<Tuple> tupletsCoveredBy(SchemafulTupleSet in, int strength) {
      Set<Tuple> ret = new HashSet<>();
      in.forEach(tuple -> ret.addAll(subtuplesOf(tuple, strength)));
      return ret;
    }

    private static List<Tuple> cartesianProduct(List<Tuple> lhs, List<Tuple> rhs) {
      return lhs.stream()
          .flatMap((Function<Tuple, Stream<Tuple>>) l -> rhs.stream()
              .map(r -> connect(l, r)))
          .collect(toList());
    }
  }
}


