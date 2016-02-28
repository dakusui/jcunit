package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.BaseBuilder;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;

import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public interface ConstraintChecker extends Plugin {
  ConstraintChecker DEFAULT_CONSTRAINT_CHECKER = new NullConstraintChecker();

  /**
   * Returns {@code true} if the given tuple doesn't violate any known constraints.
   * In case tuple doesn't have sufficient attribute values to be evaluated,
   * an {@code UndefinedSymbol} will be thrown.
   * Otherwise, {@code false} should be returned.
   *
   * @param tuple A tuple to be evaluated.
   * @return {@code true} - The tuple doesn't violate any constraints managed by this object / {@code false} - The tuple DOES violate a constraint.
   * @throws com.github.dakusui.jcunit.exceptions.UndefinedSymbol Failed to evaluate the tuple for insufficient attribute(s).
   */
  boolean check(Tuple tuple) throws UndefinedSymbol;

  List<Tuple> getViolations();

  abstract class Base implements ConstraintChecker, Plugin {
    public Base() {
    }

    @Override
    public List<Tuple> getViolations() {
      return Collections.emptyList();
    }
  }

  class Builder implements BaseBuilder<ConstraintChecker> {
    private final Class<? extends ConstraintChecker> constraintClass;
    private final RunnerContext                      runnerContext;
    private final List<Value>                        configValues;

    public Builder(Checker checker, RunnerContext runnerContext) {
      this(checker.value(), runnerContext, Utils.asList(checker.args()));
    }

    private Builder(Class<? extends ConstraintChecker> constraintClass, RunnerContext runnerContext, List<Value> configValues) {
      this.constraintClass = checknotnull(constraintClass);
      this.runnerContext = checknotnull(runnerContext);
      this.configValues = checknotnull(configValues);
    }

    /**
     * Builds a constraint checker.
     */
    @Override
    public ConstraintChecker build() {
      Factory<ConstraintChecker, Value> pluginFactory
          = Factory.newFactory(constraintClass, new Value.Resolver(), this.runnerContext);
      return pluginFactory.create(this.configValues);
    }
  }
}
