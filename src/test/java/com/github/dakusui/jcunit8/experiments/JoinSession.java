package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.IncrementalJoiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;

import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.jcunit.core.tuples.TupleUtils.project;
import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static java.util.stream.Collectors.toList;

class JoinSession {
  private final SchemafulTupleSet             lhs;
  private final SchemafulTupleSet             rhs;
  private final int                           strength;
  private final Function<Requirement, Joiner> joinerFactory;
  SchemafulTupleSet result;
  boolean executed  = false;
  boolean succeeded = false;
  private long after;
  private long before;

  JoinSession(int strength, Function<Requirement, Joiner> joinerFactory, SchemafulTupleSet lhs, SchemafulTupleSet rhs) {
    this.strength = strength;
    this.lhs = checknotnull(lhs);
    this.rhs = checknotnull(rhs);
    this.joinerFactory = checknotnull(joinerFactory);
  }

  static Joiner standard(Requirement requirement) {
    return new StandardJoiner(requirement);
  }

  static Joiner incremental(Requirement requirement) {
    return new IncrementalJoiner(requirement);
  }

  void execute() {
    Checks.checkcond(!executed);
    try {
      this.before = System.currentTimeMillis();
      this.result = CAgenerationUnderConstraints.join(strength, joinerFactory, lhs, rhs);
      this.after = System.currentTimeMillis();
      this.succeeded = true;
    } finally {
      this.executed = true;
    }
  }

  long time() {
    return this.after - this.before;
  }

  void verify() {
    List<Tuple> notUsedInLhs = notUsedTuplesIn(lhs, result);
    List<Tuple> notUsedInRhs = notUsedTuplesIn(rhs, result);
    assert notUsedInLhs.isEmpty();
    assert notUsedInRhs.isEmpty();
    assert result.size() >= rhs.size();
    assert result.size() >= lhs.size();
    assert result.size() == result.stream().distinct().collect(toList()).size();
  }

  private static List<Tuple> notUsedTuplesIn(SchemafulTupleSet lhs, SchemafulTupleSet result) {
    return lhs.stream().filter(
          each -> result.index().find(project(each, lhs.getAttributeNames())).isEmpty()
      ).collect(toList());
  }

  static class Builder {
    private final int                           strength;
    private       SchemafulTupleSet             lhs;
    private       SchemafulTupleSet             rhs;
    private       Function<Requirement, Joiner> joinerFactory;

    Builder(int strength) {
      this.strength = strength;
    }

    Builder with(Function<Requirement, Joiner> joinerFactory) {
      this.joinerFactory = joinerFactory;
      return this;
    }

    Builder lhs(SchemafulTupleSet lhs) {
      this.lhs = lhs;
      return this;
    }

    Builder rhs(SchemafulTupleSet rhs) {
      this.rhs = rhs;
      return this;
    }

    JoinSession build() {
      return new JoinSession(this.strength, joinerFactory, lhs, rhs);
    }
  }
}
