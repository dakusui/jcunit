package com.github.dakusui.jcunit.plugins.caengines;

/**
 */

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.reporters.CoverageReporter;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

/**
 * Implementations of this interface must guarantee that a public constructor without
 * any parameters exists.
 */
public interface CoveringArrayEngine extends Plugin {
  void init();

  Factors getFactors();

  void setFactors(Factors factors);

  Constraint getConstraint();

  void setConstraint(Constraint constraint);

  CoverageReporter getCoverageReporter();

  void setCoverageReporter(CoverageReporter coverageReporter);

  CoveringArray getCoveringArray();


  /**
   * A class to build a tuple generator.
   *
   * @param <S> data source type. Can be {@literal @}Param, {@code String}, etc.
   */
  class Builder<S> {

    private final Param.Resolver<S>                    resolver;
    private       Class<? extends CoveringArrayEngine> tupleGeneratorClass;
    private       Factors                              factors;
    private       Constraint                           constraint;
    private       S[]                                  parameters;
    private       RunnerContext                        runnerContext;

    public static Builder<Object> createSimpleBuilder(Object... arguments) {
      return createSimpleBuilder(IPO2CoveringArrayEngine.class, arguments);
    }

    public static Builder<Object> createSimpleBuilder(Class<? extends CoveringArrayEngine> tupleGeneratorClass, Object... arguments) {
      return new Builder<Object>(
          Param.Resolver.NULL,
          tupleGeneratorClass,
          arguments,
          Constraint.DEFAULT_CONSTRAINT_MANAGER
      ).setRunnerContext(new RunnerContext.Dummy());
    }

    public Builder(Param.Resolver<S> resolver, S... arguments) {
      this(resolver, IPO2CoveringArrayEngine.class, arguments, Constraint.DEFAULT_CONSTRAINT_MANAGER);
    }

    public Builder(
        Param.Resolver<S> resolver,
        Class<? extends CoveringArrayEngine> caEngineClass,
        S[] argumentsForCAEngine,
        Constraint cm
    ) {
      this.resolver = Checks.checknotnull(resolver);
      this.tupleGeneratorClass = Checks.checknotnull(caEngineClass);
      this.constraint = Checks.checknotnull(cm);
      this.parameters = argumentsForCAEngine;
    }

    public Builder(CoveringArrayEngine.Builder<S> base) {
      this.resolver = Checks.checknotnull(base.resolver);
      this.tupleGeneratorClass = Checks.checknotnull(base.tupleGeneratorClass);
      this.factors = Checks.checknotnull(base.factors);
      this.constraint = Checks.checknotnull(base.getConstraint());
      this.parameters = Checks.checknotnull(base.parameters);
    }

    public Builder setCAEngineClass(
        Class<? extends CoveringArrayEngine> caEngineClass) {
      this.tupleGeneratorClass = caEngineClass;
      return this;
    }

    public Builder setFactors(Factors factors) {
      this.factors = factors;
      return this;
    }

    public Builder setConstraint(Constraint constraint) {
      this.constraint = constraint;
      return this;
    }

    public Builder setParameters(S[] parameters) {
      this.parameters = parameters;
      return this;
    }

    public Builder setRunnerContext(RunnerContext runnerContext) {
      this.runnerContext = runnerContext;
      return this;
    }

    public Constraint getConstraint() {
      return constraint;
    }

    public Factors getFactors() {
      return factors;
    }

    public CoveringArrayEngine build() {
      Checks.checknotnull(this.constraint);
      Checks.checknotnull(this.tupleGeneratorClass);
      Checks.checknotnull(this.factors);
      Checks.checknotnull(this.constraint);
      Checks.checknotnull(this.runnerContext);
      Plugin.Factory<CoveringArrayEngine, S> factory = new Factory<CoveringArrayEngine, S>(
          (Class<CoveringArrayEngine>) this.tupleGeneratorClass,
          this.resolver,
          runnerContext
      );
      CoveringArrayEngine ret = factory.create(this.parameters);
      ret.setFactors(this.factors);
      ret.setConstraint(this.constraint);
      ret.init();
      return ret;
    }
  }
}
