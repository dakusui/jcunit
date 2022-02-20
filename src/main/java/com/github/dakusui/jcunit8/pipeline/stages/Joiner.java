package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.jcunit.core.tuples.Aarray;
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
        final private Function<Aarray, List<Aarray>>                                     coveredByLhs          = memoize(
            tuple -> findCoveringTuplesIn(project(tuple, lhs.getAttributeNames()), lhs)
        );
        final private Function<Aarray, List<Aarray>>                                     coveredByRhs          = memoize(
            tuple -> findCoveringTuplesIn(project(tuple, rhs.getAttributeNames()), rhs)
        );
        final private Function<Integer, Function<Aarray, Function<Aarray, Set<Aarray>>>> connectingSubtuplesOf =
            memoize(
                strength -> memoize(
                    lhsTuple -> memoize(
                        rhsTuple -> connectingSubtuplesOf(lhsTuple, rhsTuple, strength)
                    )
                )
            );

        private Optional<Aarray> findBestCombinationFor(Aarray tupleToCover, List<Aarray> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          int most = 0;
          Aarray bestLhs = null, bestRhs = null;
          for (Aarray lhsTuple : this.coveredByLhs.apply(tupleToCover)) {
            for (Aarray rhsTuple : this.coveredByRhs.apply(tupleToCover)) {
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

        private Optional<Aarray> findBestRhsFor(Aarray lhsTuple, List<Aarray> rhs, List<Aarray> alreadyUsed, TupleSet remainingTuplesToBeCovered) {
          int most = 0;
          Aarray bestRhs = null;
          for (Aarray rhsTuple : rhs) {
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
      List<Aarray> work = new LinkedList<>();
      ////
      // If there are tuples in lhs not used in work, they should be added to the
      // list. Otherwise t-way tuples covered by them will not be covered by the
      // final result. Same thing can be said in rhs.
      //
      // Modified HG (horizontal growth) procedure
      checkcond(lhs.size() >= rhs.size());
      for (int i = 0; i < lhs.size(); i++) {
        Aarray lhsTuple = lhs.get(i);
        Aarray rhsTuple = i < rhs.size() ?
            rhs.get(i) :
            session.findBestRhsFor(lhsTuple, rhs, work, remainingTuplesToBeCovered).orElse(
                rhs.get(i % rhs.size())
            );
        Aarray tuple = connect(lhsTuple, rhsTuple);
        work.add(tuple);
        remainingTuplesToBeCovered.removeAll(connectingSubtuplesOf(lhsTuple, rhsTuple, this.requirement().strength()));
      }
      ////
      // Modified VG (vertical growth) procedure
      while (!remainingTuplesToBeCovered.isEmpty()) {
        Aarray bestTuple = session.findBestCombinationFor(
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

    private List<Aarray> findCoveringTuplesIn(Aarray aTuple, SchemafulTupleSet tuples) {
      Aarray inConcern = project(aTuple, tuples.getAttributeNames());
      return tuples.stream(
      ).filter(
          inConcern::isContainedBy
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
      Set<Aarray> leftoverWorkForLhs = new LinkedHashSet<>(lhs.size());
      Set<Aarray> leftoverWorkForRhs = new LinkedHashSet<>(lhs.size());
      leftoverWorkForLhs.addAll(lhs);
      leftoverWorkForRhs.addAll(rhs);
      LinkedHashSet<Aarray> work = new LinkedHashSet<>();
      {
        Function<Function<SchemafulTupleSet, Function<Integer, Set<Aarray>>>, Function<SchemafulTupleSet, Function<Integer, SchemafulTupleSet>>> weakener
            = memoize(tupletsFinder -> memoize(in -> memoize(strength -> JoinerUtils.weakenTo(in, strength, tupletsFinder))));
        Function<SchemafulTupleSet, Function<Integer, Set<Aarray>>> tupletsFinder
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

    void addConnectedTuples(LinkedHashSet<Aarray> work, SchemafulTupleSet weakenedLhs, SchemafulTupleSet weakenedRhs) {
      work.addAll(
          JoinerUtils.cartesianProduct(
              weakenedLhs,
              weakenedRhs));
    }

    private void ensureLeftoversArePresent(
        LinkedHashSet<Aarray> b,
        Set<Aarray> leftoverWorkForLhs, Set<Aarray> leftoverWorkForRhs,
        Aarray firstTupleInLhs,
        Aarray firstTupleInRhs) {
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
        LinkedHashSet<Aarray> b,
        Set<Aarray> biggerLeftover, Set<Aarray> nonBiggerLeftover,
        Aarray firstTupleInNonBigger) {
      int max = max(biggerLeftover.size(), nonBiggerLeftover.size());
      int min = min(biggerLeftover.size(), nonBiggerLeftover.size());
      List<Aarray> leftOverFromBigger = new ArrayList<Aarray>() {{
        addAll(biggerLeftover);
      }};
      List<Aarray> leftOverFromNonBigger = new ArrayList<Aarray>() {{
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
    Set<Aarray> coveredCrossingTuplets = new HashSet<>();

    public WeakenProduct2(Requirement requirement) {
      super(requirement);
      this.composeColumnSelections = memoize((Integer i) -> memoize((List<String> l) -> memoize((List<String> r) -> composeColumnSelections(i, l, r))));
    }

    void addConnectedTuples(LinkedHashSet<Aarray> work, SchemafulTupleSet weakenedLhs, SchemafulTupleSet weakenedRhs) {
      for (Aarray eachFromLhs : weakenedLhs) {
        for (Aarray eachFromRhs : weakenedRhs) {
          int numTupletsBeforeAdding = coveredCrossingTuplets.size();
          coveredCrossingTuplets.addAll(crossingTuplets(eachFromLhs, eachFromRhs, requirement().strength()));
          if (coveredCrossingTuplets.size() > numTupletsBeforeAdding)
            work.add(connect(eachFromLhs, eachFromRhs));
        }
      }
    }

    private Collection<? extends Aarray> crossingTuplets(Aarray eachFromLhs, Aarray eachFromRhs, int strength) {
      Aarray connected = connect(eachFromLhs, eachFromRhs);
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

    static Aarray connect(Aarray tuple1, Aarray tuple2) {
      return new Aarray.Builder().putAll(tuple1).putAll(tuple2).build();
    }

    private static SchemafulTupleSet weakenTo(SchemafulTupleSet in, int strength, Function<SchemafulTupleSet, Function<Integer, Set<Aarray>>> coveredTupletsFinder) {
      SchemafulTupleSet.Builder b = new SchemafulTupleSet.Builder(in.getAttributeNames());
      Set<Aarray> tupletsToBeCovered = coveredTupletsFinder.apply(in).apply(strength);
      for (Aarray each : in) {
        int before = tupletsToBeCovered.size();
        tupletsToBeCovered.removeAll(subtuplesOf(each, strength));
        if (tupletsToBeCovered.size() < before)
          b.add(each);
        if (tupletsToBeCovered.isEmpty())
          break;
      }
      return b.build();
    }

    private static Set<Aarray> tupletsCoveredBy(SchemafulTupleSet in, int strength) {
      Set<Aarray> ret = new HashSet<>();
      in.forEach(tuple -> ret.addAll(subtuplesOf(tuple, strength)));
      return ret;
    }

    private static List<Aarray> cartesianProduct(List<Aarray> lhs, List<Aarray> rhs) {
      return lhs.stream()
          .flatMap((Function<Aarray, Stream<Aarray>>) l -> rhs.stream()
              .map(r -> connect(l, r)))
          .collect(toList());
    }
  }
}


