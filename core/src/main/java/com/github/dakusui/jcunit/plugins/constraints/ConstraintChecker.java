package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.BaseBuilder;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.annotations.Checker;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;

import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;
import static java.lang.String.format;

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

  List<Tuple> getViolations(Tuple regularTestCase);

  List<String> getTags();

  boolean violates(Tuple tuple, String constraintTag);

  List<Constraint> getConstraints();

  /**
   * Returns a new object or, in case an implementor is sure that the implementation
   * of this interface is stateless and thread safe, {@code this} object.
   */
  ConstraintChecker getFreshObject();

  /**
   * If a class that extend this is not stateless, you must override {@code getFreshObject()}
   * and define appropriate behavior,
   * because the method's implementation provided by this class always return
   * {@code this} object always.
   *
   * @see ConstraintChecker#getFreshObject
   */
  abstract class Base extends Plugin.Base implements ConstraintChecker, Plugin {
    public Base() {
    }

    @Override
    public List<Constraint> getConstraints() {
      throw new UnsupportedOperationException(
          format("This constraint checker (%s) does not allow to access individual constraints",
              this.getClass().getCanonicalName())
      );
    }

    @Override
    public List<Tuple> getViolations(Tuple regularTestCase) {
      return Collections.emptyList();
    }

    @Override
    public List<String> getTags() {
      return Collections.emptyList();
    }

    @Override
    public boolean violates(Tuple tuple, String constraintTag) {
      Checks.checknotnull(constraintTag);
      Checks.checknotnull(tuple);
      return false;
    }

    @Override
    public ConstraintChecker getFreshObject() {
      return this;
    }
  }

  class Builder implements BaseBuilder<ConstraintChecker> {
    private final Class<? extends ConstraintChecker> constraintClass;
    private final RunnerContext                      runnerContext;
    private final List<Value>                        configValues;

    public Builder(Checker checker, Class<?> testClass) {
      this(checker, new RunnerContext.Base(testClass));
    }

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
    public <CC extends ConstraintChecker> CC build() {
      Factory<ConstraintChecker, Value> pluginFactory
          = Factory.newFactory(constraintClass, new Value.Resolver(), this.runnerContext);
      //noinspection unchecked
      return (CC) pluginFactory.create(this.configValues);
    }
  }
}
