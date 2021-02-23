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

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.*;
import static com.github.dakusui.jcunit.core.utils.Checks.checkcond;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.sizeOfIntersection;
import static com.github.dakusui.jcunit8.pipeline.stages.Joiner.JoinerUtils.connect;
import static com.github.dakusui.pcond.Assertions.precondition;
import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.functions.Predicates.*;
import static java.lang.Math.max;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

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
    private final Function<SchemafulTupleSet, Function<Integer, Set<Tuple>>> tupletsCoveredBy =
        memoize(rows -> memoize(strength -> tupletsCoveredBy_(rows, strength)));

    public Set<Tuple> tupletsCoveredBy(SchemafulTupleSet rows, int strength) {
      return tupletsCoveredBy.apply(rows).apply(strength);
    }

    private Set<Tuple> tupletsCoveredBy_(SchemafulTupleSet rows, int strength) {
      long before_ = System.currentTimeMillis();
      try {
        if (strength == 1) {
          Set<Tuple> ret = new HashSet<>();
          for (Tuple each : rows)
            ret.addAll(subtuplesOf(each, 1));
          return ret;
        }
        Set<Tuple> t_1 = tupletsCoveredBy(rows, 1);
        Set<Tuple> lower = tupletsCoveredBy(rows, strength - 1);
        return lower
            .stream()
            .parallel()
            .flatMap(each -> t_1.stream().map(JoinerUtils.Pair::new).filter(t -> !each.containsKey(t.key)).map(t -> Tuple.builder().putAll(each).put(t.key, t.value).build()))
            .collect(toSet());
      } finally {
        System.err.printf("  tupletsCoveredBy(t=%s,degree=%s);time=%10d[msec]%n", strength, rows.getAttributeNames().size(), System.currentTimeMillis() - before_);
      }
    }

    public WeakenProduct(Requirement requirement) {
      super(requirement);
    }

    @Override
    protected SchemafulTupleSet doJoin(SchemafulTupleSet rawLhs, SchemafulTupleSet rawRhs) {
      if (rawRhs.isEmpty() || rawLhs.isEmpty())
        return rawLhs;
      SchemafulTupleSet lhs = JoinerUtils.shuffle(rawLhs);
      SchemafulTupleSet rhs = JoinerUtils.shuffle(rawRhs);
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
            = memoize(in -> memoize(strength -> tupletsCoveredBy(in, strength)));
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

    static void addConnectedTuples(LinkedHashSet<Tuple> work, SchemafulTupleSet weakenedLhs, SchemafulTupleSet weakenedRhs) {
      work.addAll(
          JoinerUtils.cartesianProduct(
              weakenedLhs,
              weakenedRhs));
    }

    static private void ensureLeftoversArePresent(
        LinkedHashSet<Tuple> b,
        Set<Tuple> leftoverWorkForLhs, Set<Tuple> leftoverWorkForRhs,
        Tuple firstTupleInLhs,
        Tuple firstTupleInRhs) {
      int before = b.size();
      try {
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
      } finally {
        System.err.println("numLeftOvers=" + (b.size() - before));
      }
    }

    static private void ensureLeftoversArePresent_(
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

  enum JoinerUtils {
    ;

    static Tuple connect(Tuple tuple1, Tuple tuple2) {
      return new Tuple.Builder().putAll(tuple1).putAll(tuple2).build();
    }

    private static SchemafulTupleSet weakenTo(SchemafulTupleSet in, int strength, Function<SchemafulTupleSet, Function<Integer, Set<Tuple>>> coveredTupletsFinder) {
      long before_ = System.currentTimeMillis();
      try {
        SchemafulTupleSet.Builder b = new SchemafulTupleSet.Builder(in.getAttributeNames());
        Set<Tuple> tupletsToBeCovered = coveredTupletsFinder.apply(in).apply(strength);
        long t = System.currentTimeMillis();
        for (Tuple each : in) {
          long u = System.currentTimeMillis();
          try {
            int before = tupletsToBeCovered.size();
            tupletsToBeCovered.removeAll(subtuplesOf(each, strength));
            if (tupletsToBeCovered.size() < before)
              b.add(each);
            if (tupletsToBeCovered.isEmpty())
              break;
          } finally {
            System.err.printf("      processEach(t=%s,degree=%s);time=%10d[msec]%n", strength, in.getAttributeNames().size(), System.currentTimeMillis() - u);
          }
        }
        System.err.printf("constructWeakened(t=%s,degree=%s);time=%10d[msec]%n", strength, in.getAttributeNames().size(), System.currentTimeMillis() - t);
        return b.build();
      } finally {
        System.err.printf("         weakenTo(t=%s,degree=%s);time=%10d[msec]%n", strength, in.getAttributeNames().size(), System.currentTimeMillis() - before_);
      }
    }

    static SchemafulTupleSet shuffle(SchemafulTupleSet in) {
      SchemafulTupleSet.Builder b = new SchemafulTupleSet.Builder(in.getAttributeNames());
      for (Tuple each : rowsWithLeastFrequentValuesFirst(in)) {
        b.add(each);
      }
      return b.build();
    }

    private static Iterable<Tuple> rowsWithLeastFrequentValuesFirst(SchemafulTupleSet in) {
      List<Tuple> rest = new ArrayList<>(in);
      List<Tuple> chosen = new ArrayList<>();
      return () -> new Iterator<Tuple>() {
        @Override
        public boolean hasNext() {
          return !rest.isEmpty();
        }

        @Override
        public Tuple next() {
          assert precondition(rest, not(isEmpty()));
          Tuple ret = null;
          try {
            if (rest.size() == 1) {
              ret = rest.get(0);
              return ret;
            }
            int v = -1;
            for (Tuple each : rest) {
              int cur = notCoveredValueIn(each, chosen);
              if (cur > v) {
                v = cur;
                ret = each;
              }
              if (v == in.getAttributeNames().size()) {
                break;
              }
            }
            assert that(ret, isNotNull());
            return ret;
          } finally {
            rest.remove(ret);
            chosen.add(ret);
          }
        }

        int notCoveredValueIn(Tuple row, List<Tuple> chosen1) {
          int ret = 0;
          for (String eachKey : row.keySet()) {
            int v = 1;
            for (Tuple eachChosen : chosen1) {
              if (Objects.equals(row.get(eachKey), eachChosen.get(eachKey))) {
                v = 0;
                break;
              }
            }
            ret += v;
          }
          return ret;
        }
      };
    }


    static final class Pair {
      final String key;
      final Object value;

      Pair(Tuple tuple) {
        assert tuple.size() == 1;
        this.key = tuple.keySet().iterator().next();
        this.value = tuple.get(key);
      }
    }

    public static List<Tuple> cartesianProduct(List<Tuple> lhs, List<Tuple> rhs) {
      return lhs.stream()
          .flatMap((Function<Tuple, Stream<Tuple>>) l -> rhs.stream()
              .map(r -> connect(l, r)))
          .collect(toList());
    }
  }
}


