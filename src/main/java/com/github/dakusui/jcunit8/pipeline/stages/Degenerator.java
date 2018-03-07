package com.github.dakusui.jcunit8.pipeline.stages;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;

import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static com.github.dakusui.jcunit8.core.Utils.toLinkedHashSet;

public interface Degenerator {
  SchemafulTupleSet degenerate(SchemafulTupleSet in, int doi);

  class Impl implements Degenerator {
    @Override
    public SchemafulTupleSet degenerate(SchemafulTupleSet in, int doi) {
      return null;
    }

    static class Session implements Degenerator {
      private Function<Integer, Function<Tuple, Set<Tuple>>> subtuplesOf = memoize(
          doi -> memoize(
              tuple -> TupleUtils.subtuplesOf(tuple, doi)
          ));

      @Override
      public SchemafulTupleSet degenerate(SchemafulTupleSet in, int doi) {
        TupleSet allPossibleTuples = allPossibleTuples(in, doi);
        Set<Tuple> remainingTuples = new LinkedHashSet<>(allPossibleTuples);
        Set<Tuple> remainingCandidate = in.stream().collect(toLinkedHashSet());
        SchemafulTupleSet.Builder b = new SchemafulTupleSet.Builder(in.getAttributeNames());
        long removedLastTime = -1;
        while (!remainingTuples.isEmpty()) {
          long max = removedLastTime == -1 ?
              new Combinator<>(in.getAttributeNames(), doi).size() :
              removedLastTime;
          Tuple chosen = findBestTuple(remainingCandidate, max, doi,
              remainingTuples
          ).orElseThrow(RuntimeException::new);
          remainingCandidate.remove(chosen);
          long before = remainingTuples.size();
          remainingTuples.removeAll(subtuplesOf(chosen, doi));
          removedLastTime = before - remainingTuples.size();
        }
        return b.build();
      }

      private static TupleSet allPossibleTuples(SchemafulTupleSet in, int doi) {
        return new TupleSet.Builder() {{
          Utils.combinations(in.getAttributeNames(), doi).forEach(
              factorNames ->
                  in.stream().map(
                      each -> TupleUtils.project(each, factorNames)
                  ).forEach(
                      this::add
                  )
          );
        }}.build();
      }

      private Optional<Tuple> findBestTuple(Set<Tuple> in, long possibleMax, int doi, Set<Tuple> remainingTuples) {
        return Utils.max(
            in.stream(),
            possibleMax,
            tuple -> (long) Utils.intersection(subtuplesOf(tuple, doi), remainingTuples).size()
        );
      }

      private Set<Tuple> subtuplesOf(Tuple tuple, int doi) {
        return subtuplesOf.apply(doi).apply(tuple);
      }
    }
  }
}
