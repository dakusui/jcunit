package com.github.dakusui.jcunit.plugins.caengines;

/**
 */

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.reporters.CoverageReporter;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checkcond;
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

    public List<S> getConfigArgsForEngine() {
      return configArgsForEngine;
    }

    public Param.Resolver<S> getResolver() {
      return resolver;
    }
  }

  /**
   * An abstract base class that provides a basic implementation of {@code CAEngine}.
   * Users can create a new tuple generator by extending this class.
   */
  abstract class Base
      implements CoveringArrayEngine, Plugin {

    private Factors factors = null;
    private Constraint constraint;
    private CoverageReporter coverageReporter = new CoverageReporter.Default();
    private CoveringArray coveringArray;

    public Base() {
    }

    final public void init() {
      checkcond(factors != null, "Factors must be set in advance");
      checkcond(constraint != null, "Constraint must be set in advance");
      checkcond(coverageReporter != null, "Coverage reporter must be set in advance");
      this.coveringArray = createCoveringArray(generate());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public void setFactors(Factors factors) {
      this.factors = checknotnull(factors);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public Factors getFactors() {
      return this.factors;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    final public void setConstraint(Constraint constraint) {
      this.constraint = checknotnull(constraint);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Constraint getConstraint() {
      return this.constraint;
    }

    @Override
    public CoverageReporter getCoverageReporter() {
      return this.coverageReporter;
    }

    @Override
    public void setCoverageReporter(CoverageReporter coverageReporter) {
      this.coverageReporter = Checks.checknotnull(coverageReporter);
    }

    @Override
    public CoveringArray getCoveringArray() {
      checkcond(
          this.coveringArray != null,
          "'generate' method should be called before this method is invoked.");
      return this.coveringArray;
    }

    /**
     * Implementation of this method must return a list of tuples (test cases)
     * generated by this object.
     * <p/>
     */
    abstract protected List<Tuple> generate();

    protected CoveringArray createCoveringArray(List<Tuple> testCase) {
      return new CoveringArray.Base(testCase);
    }

  }
}
