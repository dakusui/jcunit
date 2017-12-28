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

  @Override
  public String toString() {
    return String.format(
        "size=%d; width=%d; time=%s[msec]; %s (input=lhs=%s; rhs=%s)",
        this.result.size(), this.result.width(),
        this.time(), this.result.getAttributeNames().size(),
        lhs.size(),
        rhs.size()
    );
  }

  static Joiner standard(Requirement requirement) {
    return new StandardJoiner(requirement);
  }

  static Joiner incremental(Requirement requirement) {
    return new IncrementalJoiner(requirement);
  }

  void verify() {
    List<Tuple> notUsedInLhs = notUsedTuplesIn(lhs, result);
    List<Tuple> notUsedInRhs = notUsedTuplesIn(rhs, result);
    assert notUsedInLhs.isEmpty() : String.format("%d:%s%n%d:%s%n%d:%s%n", notUsedInLhs.size(), notUsedInLhs, lhs.size(), lhs, result.size(), result.project(lhs.getAttributeNames()));
    assert notUsedInRhs.isEmpty() : String.format("%d:%s%n%d:%s%n%d:%s%n", notUsedInRhs.size(), notUsedInRhs, rhs.size(), rhs, result.size(), result.project(rhs.getAttributeNames()));
    assert result.size() >= rhs.size();
    assert result.size() >= lhs.size();
    assert result.size() == result.stream().distinct().collect(toList()).size();
  }

  private static List<Tuple> notUsedTuplesIn(SchemafulTupleSet input, SchemafulTupleSet result) {
    return input.stream().filter(
        eachFromInput -> !result.project(input.getAttributeNames()).contains(eachFromInput)
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
