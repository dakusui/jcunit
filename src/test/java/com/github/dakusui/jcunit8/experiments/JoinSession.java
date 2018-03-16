package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.Florence;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.Lucas;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner;
import com.github.dakusui.jcunit8.pipeline.stages.joiners.old.IncrementalJoiner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static com.github.dakusui.jcunit8.core.Utils.memoize;
import static java.util.stream.Collectors.toList;

class JoinSession {
  private final SchemafulTupleSet             lhs;
  private final SchemafulTupleSet             rhs;
  private final int                           strength;
  private final Function<Requirement, Joiner> joinerFactory;
  SchemafulTupleSet result;
  private boolean executed  = false;
  private       long    after;
  private       long    before;
  private final boolean fullCheck;
  private final Function<SchemafulTupleSet, Function<Set<String>, SchemafulTupleSet.Index>> project = memoize(
      tuples -> memoize(
          factors -> project_(tuples, factors)
      ));

  JoinSession(int strength, Function<Requirement, Joiner> joinerFactory, SchemafulTupleSet lhs, SchemafulTupleSet rhs, boolean fullCheck) {
    this.strength = strength;
    this.lhs = checknotnull(lhs);
    this.rhs = checknotnull(rhs);
    this.joinerFactory = checknotnull(joinerFactory);
    this.fullCheck = fullCheck;
  }

  void execute() {
    Checks.checkcond(!executed);
    try {
      this.before = System.currentTimeMillis();
      this.result = CAgenerationUnderConstraints.join(strength, joinerFactory, lhs, rhs);
      this.after = System.currentTimeMillis();
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

  static Joiner florence(Requirement requirement) {
    return new Florence(requirement);
  }

  static Joiner lucas(Requirement requirement) {
    return new Lucas(requirement);
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
    int missingTuplesTrigger = 100;
    if (fullCheck)
      verifyCoverage(missingTuplesTrigger);
  }

  private void verifyCoverage(int trigger) {
    Florence.debug("Verifying...");
    List<Tuple> missingTuples = new LinkedList<Tuple>() {
      @Override
      public boolean add(Tuple entry) {
        boolean ret = super.add(entry);
        if (size() > trigger)
          throw new RuntimeException("Too many missing tuples!");
        return ret;
      }
    };
    verifyCoverage(missingTuples::add);
    if (!missingTuples.isEmpty()) {
      missingTuples.forEach(System.err::println);
      throw new RuntimeException("Missing tuples are found!");
    }
    Florence.debug("Verification finished successfully");
  }

  private void verifyCoverage(Consumer<Tuple> missingTupleHandler) {
    IntStream.range(0, this.strength).boxed().flatMap(
        i -> Utils.combinations(lhs.getAttributeNames(), i).flatMap(
            factorsFromLhs -> lhs.project(factorsFromLhs).stream().flatMap(
                tupleFromLhs -> Utils.combinations(rhs.getAttributeNames(), strength - i).flatMap(
                    factorsFromRhs -> rhs.project(factorsFromRhs).stream().map(
                        tupleFromRhs -> TupleUtils.connect(tupleFromLhs, tupleFromRhs)
                    ))))
    ).filter(
        tuple -> !isCoveredBy(tuple, result)
    ).forEach(missingTupleHandler);
  }

  private boolean isCoveredBy(Tuple tuple, SchemafulTupleSet tuples) {
    return !project(tuples, tuple.keySet()).find(tuple).isEmpty();
  }

  private SchemafulTupleSet.Index project(SchemafulTupleSet tuples, Set<String> factors) {
    return project.apply(tuples).apply(factors);
  }

  private SchemafulTupleSet.Index project_(SchemafulTupleSet tuples, Set<String> factors) {
    return tuples.project(new ArrayList<>(factors)).index();
  }

  private static List<Tuple> notUsedTuplesIn(SchemafulTupleSet
      input, SchemafulTupleSet result) {
    return input.stream().filter(
        eachFromInput -> !result.project(input.getAttributeNames()).contains(eachFromInput)
    ).collect(toList());
  }

  static class Builder {
    private final int                           strength;
    private       SchemafulTupleSet             lhs;
    private       SchemafulTupleSet             rhs;
    private       Function<Requirement, Joiner> joinerFactory;
    private       boolean                       fullCheck;

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

    public Builder fullCheck(boolean fullCheck) {
      this.fullCheck = fullCheck;
      return this;
    }

    JoinSession build() {
      return new JoinSession(this.strength, joinerFactory, lhs, rhs, fullCheck);
    }
  }
}
