package com.github.dakusui.jcunit8.pipeline.stages.joiners.old;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.LinkedHashSet;
import java.util.Optional;
import java.util.Set;

import static java.util.Comparator.comparingInt;
import static java.util.Objects.requireNonNull;

public class DegenerationJoiner extends Joiner.Base {

  private final Requirement requirement;

  Degenerator degenerator;

  public DegenerationJoiner(Requirement requirement) {
    this.requirement = requireNonNull(requirement);
    this.degenerator = new Degenerator();
  }

  /*
   * |lhs| >= |rhs|
   */
  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    return null;
  }

  private Set<Tuple> limitedJoin(SchemafulTupleSet lhs, int lhsStrength, SchemafulTupleSet rhs, int rhsStrength) {
    TupleSet tuplesToBeCovered = lhs.subtuplesOf(lhsStrength).cartesianProduct(rhs.subtuplesOf(rhsStrength));


    assert tuplesToBeCovered.isEmpty();
    return null;
  }

  static class Degenerator {
    /**
     * Returns degenerated set of tuples from a given set of tuples.
     *
     * @param tuplesToBeCovered Contents of this set will be modified after a call.
     * @param tuples            tuples used in a returned set to cover {@code tuplesToBeCovered}.
     * @param mandatory         tuples included in a returned set mandatory.
     * @return A set of tuple that covers all of {@code tuplesToBeCovered}.
     */
    Optional<Set<Tuple>> degenerate(int strength, Set<Tuple> tuplesToBeCovered, Set<Tuple> tuples, Set<Tuple> mandatory) {
      assert tuples.containsAll(mandatory);
      assert !tuples.isEmpty();
      assert tuples.stream().noneMatch(i -> i.size() != strength);

      TupleSet.Builder builder = new TupleSet.Builder();
      mandatory.forEach(i -> {
        tuplesToBeCovered.removeAll(TupleUtils.subtuplesOf(i, strength));
        builder.add(i);
      });
      while (!tuples.isEmpty()) {
        Optional<Tuple> chosen = choose(tuplesToBeCovered, tuples);
        if (chosen.isPresent()) {
          tuplesToBeCovered.removeAll(TupleUtils.subtuplesOf(chosen.get(), strength));
        } else {
          break;
        }
      }
      return tuplesToBeCovered.isEmpty() ?
          Optional.of(builder.build()) :
          Optional.empty();
    }

    Optional<Tuple> choose(Set<Tuple> tuplesToBeCovered, Set<Tuple> tuples) {
      int strength = tuples.iterator().next().size();
      return tuples.stream()
          .filter(each -> !Helper.intersection(TupleUtils.subtuplesOf(each, strength), tuplesToBeCovered).isEmpty())
          .max(
              comparingInt(o -> Helper.intersection(TupleUtils.subtuplesOf(o, strength), tuplesToBeCovered).size())
          );
    }

    enum Helper {
      ;

      static Set<Tuple> intersection(Set<Tuple> a, Set<Tuple> b) {
        if (a.size() > b.size()) {
          Set<Tuple> c = b;
          b = a;
          a = c;
        }
        return _intersection(a, b);
      }

      private static Set<Tuple> _intersection(Set<Tuple> a, Set<Tuple> b) {
        return new LinkedHashSet<Tuple>() {{
          a.forEach(
              each -> {
                if (b.contains(each))
                  add(each);
              }
          );
        }};
      }
    }
  }

  class Session {

  }
}
