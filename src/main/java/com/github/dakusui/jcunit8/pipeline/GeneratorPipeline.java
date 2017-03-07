package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit8.core.Config;
import com.github.dakusui.jcunit8.core.Requirement;
import com.github.dakusui.jcunit8.model.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.model.parameterspace.ParameterSpace;
import com.github.dakusui.jcunit8.model.testsuite.TestSuite;
import com.github.dakusui.jcunit8.model.testsuite.TupleSuite;

import java.util.List;
import java.util.function.Function;

import static java.util.stream.Collectors.toList;

/**
 * A pipeline object.
 */
public interface GeneratorPipeline<T> {
  Function<ParameterSpace<T>, List<FactorSpace>> encoder();

  Function<List<FactorSpace>, List<FactorSpace>> partitioner();

  Function<FactorSpace, TupleSuite> generator(Requirement requirement);

  Function<TupleSuite, TupleSuite> phantomEliminator(ParameterSpace parameterSpace);

  Function<List<TupleSuite>, TupleSuite> joiner();

  Function<TupleSuite, TestSuite<T>> concretizer();

  TestSuite<T> execute(Config config, ParameterSpace<T> parameterSpace);

  abstract class Base<T> implements GeneratorPipeline<T> {
    @Override
    public TestSuite<T> execute(Config config, ParameterSpace<T> parameterSpace) {
      return concretizer().apply(
          phantomEliminator(parameterSpace).apply(
              joiner().apply(
                  partitioner().apply(encoder()
                      .apply(parameterSpace)
                  ).stream()
                      .map(generator(config.getRequirement()))
                      .map(phantomEliminator(parameterSpace))
                      .collect(toList())
              )));
    }
  }

  class Builder<T> {
    GeneratorPipeline<T> build() {
      return null;
    }
  }
}
