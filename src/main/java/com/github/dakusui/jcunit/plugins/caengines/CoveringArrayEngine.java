package com.github.dakusui.jcunit.plugins.caengines;

/**
 */

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.reporters.CoverageReporter;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

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

    private RunnerContext                        runnerContext;
    private Factors                              factors;
    private Constraint                           constraint;
    private Param.Resolver<S>                    resolver;
    private List<S>                              configArgsForEngine;
    private Class<? extends CoveringArrayEngine> engineClass;

    public Builder(
        RunnerContext runnerContext,
        Factors factors, Constraint cm, Class<? extends CoveringArrayEngine> engineClass
    ) {
      this.runnerContext = checknotnull(runnerContext);
      this.factors = factors;
      this.engineClass = checknotnull(engineClass);
      this.constraint = checknotnull(cm);
    }

    public Builder setConfigArgsForEngine(List<S> parameters) {
      this.configArgsForEngine = parameters;
      return this;
    }

    public Builder setResolver(Param.Resolver<S> resolver) {
      this.resolver = resolver;
      return this;
    }


    public Factors getFactors() {
      return factors;
    }

    public Constraint getConstraint() {
      return constraint;
    }

    public CoveringArrayEngine build() {
      checknotnull(this.constraint);
      checknotnull(this.engineClass);
      checknotnull(this.factors);
      checknotnull(this.constraint);
      checknotnull(this.runnerContext);
      CoveringArrayEngine ret;
      Plugin.Factory<CoveringArrayEngine, S> factory;
      List<S> configArgsForEngine;
      if (this.resolver != null) {
        // PECS cast: Factory uses a class object as a consumer.
        //noinspection unchecked
        factory = new Factory<CoveringArrayEngine, S>(
            (Class<? super CoveringArrayEngine>) this.engineClass,
            this.resolver,
            runnerContext
        );
        configArgsForEngine = this.configArgsForEngine;
      } else {
        // PassThroughResolver is always safe.
        //noinspection unchecked
        factory = new Factory<CoveringArrayEngine, S>(
            (Class<? super CoveringArrayEngine>) this.engineClass,
            (Param.Resolver<S>) Param.Resolver.passThroughResolver(),
            runnerContext
        );
        configArgsForEngine = Collections.emptyList();
      }
      ret = factory.create(configArgsForEngine);
      ret.setFactors(this.factors);
      ret.setConstraint(this.constraint);
      ret.init();
      return ret;
    }

    public Class<? extends CoveringArrayEngine> getEngineClass() {
      return engineClass;
    }
  }
}
