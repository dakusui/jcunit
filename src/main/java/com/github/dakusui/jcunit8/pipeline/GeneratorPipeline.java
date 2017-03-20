package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Config;
import com.github.dakusui.jcunit8.core.Requirement;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import com.github.dakusui.jcunit8.testsuite.TupleSuite;

import java.util.List;
import java.util.Optional;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

/**
 * A pipeline object.
 */
public interface GeneratorPipeline<T> {
  /**
   * Returns a function that encodes a parameter space into internal factor spaces.
   */
  Function<ParameterSpace, List<FactorSpace.Internal>> encoder();

  Function<List<FactorSpace.Internal>, List<FactorSpace.Internal>> partitioner();

  Function<FactorSpace.Internal, TupleSuite> generator(Requirement requirement);

  BinaryOperator<TupleSuite> joiner();

  Function<Tuple, T> concretizer();

  TestSuite<T> execute(Config config, ParameterSpace parameterSpace);

  abstract class Base<T> implements GeneratorPipeline<T> {
    @Override
    public TestSuite<T> execute(Config config, ParameterSpace parameterSpace) {
      Optional<TupleSuite> tupleSuite = partitioner().apply(encoder().apply(parameterSpace))
          .stream()
          .map(generator(config.getRequirement()))
          .reduce(joiner());
      if (!tupleSuite.isPresent()) {
        throw new RuntimeException();
      }
      TestSuite.Builder<T> builder = new TestSuite.Builder<T>(parameterSpace, concretizer());
      tupleSuite.get().forEach(builder::add);
      return builder.build();
    }
  }

  class Builder<T> {
    private final Generator.Factory  generatorFactory;
    private final Function<Tuple, T> concretizer;

    public static Builder<Tuple> createPassthroughBuilder(Generator.Factory generatorFactory) {
      return new Builder<>(generatorFactory, tuple -> tuple);
    }

    public Builder(Generator.Factory generatorFactory, Function<Tuple, T> concretizer) {
      this.generatorFactory = requireNonNull(generatorFactory);
      this.concretizer = requireNonNull(concretizer);
    }

    public GeneratorPipeline<T> build() {
      return new GeneratorPipeline.Base<T>() {
        @Override
        public Function<ParameterSpace, List<FactorSpace.Internal>> encoder() {
          return (ParameterSpace parameterSpace) -> parameterSpace.getParameterNames().stream()
              .map((Function<String, Parameter>) parameterSpace::getParameter)
              .map(Parameter::toInternalFactorSpace)
              .collect(toList());
        }

        @Override
        public Function<List<FactorSpace.Internal>, List<FactorSpace.Internal>> partitioner() {
          return null;
        }

        @Override
        public Function<FactorSpace.Internal, TupleSuite> generator(Requirement requirement) {
          return (FactorSpace.Internal factorSpace) -> TupleSuite.fromTuples(generatorFactory.create(factorSpace, requirement).generate());
        }

        @Override
        public BinaryOperator<TupleSuite> joiner() {
          return null;
        }

        @Override
        public Function<Tuple, T> concretizer() {
          return concretizer;
        }
      };
    }
  }
}
