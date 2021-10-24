package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit.core.tuples.Row;
import com.github.dakusui.jcunit8.core.StreamableCombinator;
import com.github.dakusui.jcunit.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulRowSet;
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

public interface Joiner extends BinaryOperator<SchemafulRowSet> {
  abstract class Base implements Joiner {
    private final Requirement requirement;

    protected Base(Requirement requirement) {
      this.requirement = requirement;
    }

    @Override
    public SchemafulRowSet apply(SchemafulRowSet lhs, SchemafulRowSet rhs) {
      FrameworkException.checkCondition(Collections.disjoint(lhs.getAttributeNames(), rhs.getAttributeNames()));
      if (lhs.isEmpty() || rhs.isEmpty())
        return emptyTupleSet(lhs, rhs);
      if (lhs.size() > rhs.size())
        return doJoin(lhs, rhs);
      return doJoin(rhs, lhs);
    }

    private SchemafulRowSet emptyTupleSet(SchemafulRowSet lhs, SchemafulRowSet rhs) {
      return SchemafulRowSet.empty(new LinkedList<String>() {{
        addAll(lhs.getAttributeNames());
        addAll(rhs.getAttributeNames());
      }});
    }

    protected Requirement requirement() {
      return this.requirement;
    }

    protected abstract SchemafulRowSet doJoin(SchemafulRowSet lhs, SchemafulRowSet rhs);
  }

  class Standard extends Base {

    public Standard(Requirement requirement) {
      super(requirement);
    }

    @Override
    protected SchemafulRowSet doJoin(SchemafulRowSet lhs, SchemafulRowSet rhs) {
      class Session {
        final private Function<KeyValuePairs, List<KeyValuePairs>>                                            coveredByLhs          = memoize(
            tuple -> findCoveringTuplesIn(project(tuple, lhs.getAttributeNames()), lhs)
        );
        final private Function<KeyValuePairs, List<KeyValuePairs>>                                            coveredByRhs          = memoize(
            tuple -> findCoveringTuplesIn(project(tuple, rhs.getAttributeNames()), rhs)
        );
        final private Function<Integer, Function<KeyValuePairs, Function<KeyValuePairs, Set<KeyValuePairs>>>> connectingSubtuplesOf =
            memoize(
                strength -> memoize(
                    lhsTuple -> memoize(
                        rhsTuple -> connectingSubtuplesOf(lhsTuple, rhsTuple, strength)
                    )
                )
            );

        private Optional<KeyValuePairs> findBestCombinationFor(KeyValuePairs tupleToCover, List<? extends KeyValuePairs> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          int most = 0;
          KeyValuePairs bestLhs = null, bestRhs = null;
          for (KeyValuePairs lhsTuple : this.coveredByLhs.apply(tupleToCover)) {
            for (KeyValuePairs rhsTuple : this.coveredByRhs.apply(tupleToCover)) {
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

        private Optional<KeyValuePairs> findBestRhsFor(KeyValuePairs lhsTuple, List<? extends KeyValuePairs> rhs, List<? extends KeyValuePairs> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          int most = 0;
          KeyValuePairs bestRhs = null;
          for (KeyValuePairs rhsTuple : rhs) {
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
      List<Row> work = new LinkedList<>();
      ////
      // If there are tuples in lhs not used in work, they should be added to the
      // list. Otherwise t-way tuples covered by them will not be covered by the
      // final result. Same thing can be said in rhs.
      //
      // Modified HG (horizontal growth) procedure
      checkcond(lhs.size() >= rhs.size());
      for (int i = 0; i < lhs.size(); i++) {
        KeyValuePairs lhsTuple = lhs.get(i);
        KeyValuePairs rhsTuple = i < rhs.size() ?
            rhs.get(i) :
            session.findBestRhsFor(lhsTuple, rhs, work, remainingTuplesToBeCovered).orElse(
                rhs.get(i % rhs.size())
            );
        Row tuple = Row.from(connect(lhsTuple, rhsTuple));
        work.add(tuple);
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(lhsTuple, rhsTuple, this.requirement().strength()));
      }
      ////
      // Modified VG (vertical growth) procedure
      while (!remainingTuplesToBeCovered.isEmpty()) {
        KeyValuePairs bestTuple = session.findBestCombinationFor(
            remainingTuplesToBeCovered.stream().findFirst().orElseThrow(
                IllegalStateException::new
            ),
            work,
            remainingTuplesToBeCovered
        ).orElseThrow(
            IllegalStateException::new
        );

        work.add(Row.from(bestTuple));
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(
            project(bestTuple, lhs.getAttributeNames()),
            project(bestTuple, rhs.getAttributeNames()),
            requirement().strength()
        ));
      }
      return new SchemafulRowSet.Builder(
          Stream.concat(
              lhs.getAttributeNames().stream(),
              rhs.getAttributeNames().stream()
          ).collect(toList()))
          .addAll(work)
          .build();
    }

    private List<KeyValuePairs> findCoveringTuplesIn(KeyValuePairs aTuple, SchemafulRowSet tuples) {
      KeyValuePairs inConcern = project(aTuple, tuples.getAttributeNames());
      return tuples.stream(
      ).filter(
          inConcern::isSubtupleOf
      ).collect(
          toList()
      );
    }

    private static TupleSet computeTuplesToBeCovered(SchemafulRowSet lhs, SchemafulRowSet rhs, int strength) {
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
    protected SchemafulRowSet doJoin(SchemafulRowSet lhs, SchemafulRowSet rhs) {
      if (rhs.isEmpty() || lhs.isEmpty())
        return lhs;

      SchemafulRowSet.Builder b = new SchemafulRowSet.Builder(new ArrayList<String>() {{
        addAll(lhs.getAttributeNames());
        addAll(rhs.getAttributeNames());
      }});
      Set<KeyValuePairs> leftoverWorkForLhs = new LinkedHashSet<>(lhs.size());
      Set<KeyValuePairs> leftoverWorkForRhs = new LinkedHashSet<>(lhs.size());
      leftoverWorkForLhs.addAll(lhs);
      leftoverWorkForRhs.addAll(rhs);
      LinkedHashSet<KeyValuePairs> work = new LinkedHashSet<>();
      {
        Function<Function<SchemafulRowSet, Function<Integer, Set<KeyValuePairs>>>, Function<SchemafulRowSet, Function<Integer, SchemafulRowSet>>> weakener
            = memoize(tupletsFinder -> memoize(in -> memoize(strength -> JoinerUtils.weakenTo(in, strength, tupletsFinder))));
        Function<SchemafulRowSet, Function<Integer, Set<KeyValuePairs>>> tupletsFinder
            = memoize(in -> memoize(strength -> JoinerUtils.tupletsCoveredBy(in, strength)));
        for (int i = 1; i < requirement().strength(); i++) {
          SchemafulRowSet weakenedLhs = weakener.apply(tupletsFinder).apply(lhs).apply(i);
          SchemafulRowSet weakenedRhs = weakener.apply(tupletsFinder).apply(rhs).apply(requirement().strength() - i);
          addConnectedTuples(work, weakenedLhs, weakenedRhs);
          leftoverWorkForLhs.removeAll(weakenedLhs);
          leftoverWorkForRhs.removeAll(weakenedRhs);
        }
      }

      ensureLeftoversArePresent(work,
          leftoverWorkForLhs, leftoverWorkForRhs,
          lhs.get(0), rhs.get(0));
      b.addAll(work.stream().map(Row::from).collect(toList()));
      return b.build();
    }

    void addConnectedTuples(LinkedHashSet<KeyValuePairs> work, SchemafulRowSet weakenedLhs, SchemafulRowSet weakenedRhs) {
      work.addAll(
          JoinerUtils.cartesianProduct(
              weakenedLhs,
              weakenedRhs));
    }

    private void ensureLeftoversArePresent(
        LinkedHashSet<KeyValuePairs> b,
        Set<KeyValuePairs> leftoverWorkForLhs, Set<KeyValuePairs> leftoverWorkForRhs,
        KeyValuePairs firstTupleInLhs,
        KeyValuePairs firstTupleInRhs) {
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
        LinkedHashSet<KeyValuePairs> b,
        Set<KeyValuePairs> biggerLeftover, Set<KeyValuePairs> nonBiggerLeftover,
        KeyValuePairs firstTupleInNonBigger) {
      int max = max(biggerLeftover.size(), nonBiggerLeftover.size());
      int min = min(biggerLeftover.size(), nonBiggerLeftover.size());
      List<KeyValuePairs> leftOverFromBigger = new ArrayList<KeyValuePairs>() {{
        addAll(biggerLeftover);
      }};
      List<KeyValuePairs> leftOverFromNonBigger = new ArrayList<KeyValuePairs>() {{
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
    Set<KeyValuePairs> coveredCrossingTuplets = new HashSet<>();

    public WeakenProduct2(Requirement requirement) {
      super(requirement);
      this.composeColumnSelections = memoize((Integer i) -> memoize((List<String> l) -> memoize((List<String> r) -> composeColumnSelections(i, l, r))));
    }

    void addConnectedTuples(LinkedHashSet<KeyValuePairs> work, SchemafulRowSet weakenedLhs, SchemafulRowSet weakenedRhs) {
      for (KeyValuePairs eachFromLhs : weakenedLhs) {
        for (KeyValuePairs eachFromRhs : weakenedRhs) {
          int numTupletsBeforeAdding = coveredCrossingTuplets.size();
          coveredCrossingTuplets.addAll(crossingTuplets(eachFromLhs, eachFromRhs, requirement().strength()));
          if (coveredCrossingTuplets.size() > numTupletsBeforeAdding)
            work.add(connect(eachFromLhs, eachFromRhs));
        }
      }
    }

    private Collection<? extends KeyValuePairs> crossingTuplets(KeyValuePairs eachFromLhs, KeyValuePairs eachFromRhs, int strength) {
      KeyValuePairs connected = connect(eachFromLhs, eachFromRhs);
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

    static KeyValuePairs connect(KeyValuePairs tuple1, KeyValuePairs tuple2) {
      return new KeyValuePairs.Builder().putAll(tuple1).putAll(tuple2).buildTuple();
    }

    private static SchemafulRowSet weakenTo(SchemafulRowSet in, int strength, Function<SchemafulRowSet, Function<Integer, Set<KeyValuePairs>>> coveredTupletsFinder) {
      SchemafulRowSet.Builder b = new SchemafulRowSet.Builder(in.getAttributeNames());
      Set<KeyValuePairs> tupletsToBeCovered = coveredTupletsFinder.apply(in).apply(strength);
      for (Row each : in) {
        int before = tupletsToBeCovered.size();
        tupletsToBeCovered.removeAll(subtuplesOf(each, strength));
        if (tupletsToBeCovered.size() < before)
          b.add(each);
        if (tupletsToBeCovered.isEmpty())
          break;
      }
      return b.build();
    }

    private static Set<KeyValuePairs> tupletsCoveredBy(SchemafulRowSet in, int strength) {
      Set<KeyValuePairs> ret = new HashSet<>();
      in.forEach(tuple -> ret.addAll(subtuplesOf(tuple, strength)));
      return ret;
    }

    private static List<KeyValuePairs> cartesianProduct(List<? extends KeyValuePairs> lhs, List<? extends KeyValuePairs> rhs) {
      return lhs.stream()
          .flatMap((Function<KeyValuePairs, Stream<KeyValuePairs>>) l -> rhs.stream()
              .map(r -> connect(l, r)))
          .collect(toList());
    }
  }
}


