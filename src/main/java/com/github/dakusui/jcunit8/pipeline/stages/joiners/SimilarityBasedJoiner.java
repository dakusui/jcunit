package com.github.dakusui.jcunit8.pipeline.stages.joiners;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TupleSet;

import java.util.LinkedHashSet;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public class SimilarityBasedJoiner extends Joiner.Base {
  private final Requirement requirement;

  public SimilarityBasedJoiner(Requirement requirement) {
    this.requirement = requireNonNull(requirement);
  }

  @Override
  protected SchemafulTupleSet doJoin(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    Session session = new Session(lhs, rhs);
    Set<Tuple> work = new LinkedHashSet<>();
    int d = this.requirement.strength();

    ////
    // Prepare rhs sub-tuple set of specified strength
    TupleSet subtuplesFromRhs = rhs.subtuplesOf(d - 1);

    return new SchemafulTupleSet.Builder(
        Stream.concat(
            lhs.getAttributeNames().stream(),
            rhs.getAttributeNames().stream()
        ).collect(
            toList()
        )
    ).addAll(work).build();
  }

  static class Session {
    final SchemafulTupleSet lhs;
    final SchemafulTupleSet rhs;

    Session(SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
      this.lhs = lhs;
      this.rhs = rhs;
    }
  }

  enum Helper {
    ;

    static double similarity(Tuple t, Tuple u) {
      Tuple v;
      if (t.size() < u.size()) {
        v = t;
        t = u;
        u = v;
      }
      double ret = 0;
      for (String k : u.keySet())
        ret += Objects.equals(t.get(k), u.get(k)) ?
            1 :
            0;
      ret = ret / (double) u.keySet().size();
      return ret;
    }
  }
}
