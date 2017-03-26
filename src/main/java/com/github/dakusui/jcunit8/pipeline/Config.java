package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.*;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.testsuite.TupleSuite;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Config<T> {
  Requirement getRequirement();

  /**
   * Returns a function that encodes a parameter space into internal factor spaces.
   */
  Function<ParameterSpace, List<FactorSpace>> encoder();

  Function<List<FactorSpace>, List<FactorSpace>> partitioner();

  Function<FactorSpace, TupleSuite> generator(Requirement requirement);

  BinaryOperator<TupleSuite> joiner();

  Function<Tuple, T> concretizer();

  Function<? super FactorSpace, ? extends FactorSpace> optimizer();

  class Builder<T> {
    private final Requirement        requirement;
    private       Generator.Factory  generatorFactory;
    private       Function<Tuple, T> concretizer;
    private       Joiner             joiner;
    private       Partitioner        partitioner;

    public static Builder<Tuple> forTuple(Requirement requirement) {
      return new Builder<Tuple>(requirement).withConcretizer(tuple -> tuple);
    }

    public Builder(Requirement requirement) {
      this.requirement = requirement;
      this.generatorFactory = new Generator.Factory.Standard();
      this.joiner = new Joiner.Standard(requirement);
      this.partitioner = new Partitioner.Standard();
    }

    public Builder<T> withGeneratorFactory(Generator.Factory generatorFactory) {
      this.generatorFactory = generatorFactory;
      return this;
    }

    public Builder<T> withConcretizer(Function<Tuple, T> concretizer) {
      this.concretizer = concretizer;
      return this;
    }

    public Builder<T> withJoiner(Joiner joiner) {
      this.joiner = joiner;
      return this;
    }

    public Builder<T> withPartitioner(Partitioner partitioner) {
      this.partitioner = partitioner;
      return this;
    }

    public Config<T> build() {
      return new Impl<>(requirement, generatorFactory, concretizer, joiner, partitioner);
    }
  }

  class Impl<T> implements Config<T> {
    private final Generator.Factory  generatorFactory;
    private final Function<Tuple, T> concretizer;
    private final Joiner             joiner;
    private final Partitioner        partitioner;
    private final Requirement        requirement;

    public Impl(Requirement requirement, Generator.Factory generatorFactory, Function<Tuple, T> concretizer, Joiner joiner, Partitioner partitioner) {
      this.generatorFactory = requireNonNull(generatorFactory);
      this.concretizer = requireNonNull(concretizer);
      this.joiner = requireNonNull(joiner);
      this.partitioner = requireNonNull(partitioner);
      this.requirement = requireNonNull(requirement);
    }

    @Override
    public Function<ParameterSpace, List<FactorSpace>> encoder() {
      return (ParameterSpace parameterSpace) -> parameterSpace.getParameterNames().stream()
          .map((Function<String, Parameter>) parameterSpace::getParameter)
          .map(Parameter::toFactorSpace)
          .collect(toList());
    }

    @Override
    public Function<List<FactorSpace>, List<FactorSpace>> partitioner() {
      return partitioner;
    }

    @Override
    public Function<FactorSpace, TupleSuite> generator(Requirement requirement) {
      return (FactorSpace factorSpace) -> TupleSuite.fromTuples(generatorFactory.create(emptyList(), factorSpace, requirement).generate());
    }

    @Override
    public BinaryOperator<TupleSuite> joiner() {
      return joiner;
    }

    @Override
    public Function<Tuple, T> concretizer() {
      return concretizer;
    }

    @Override
    public Requirement getRequirement() {
      return requirement;
    }

    /**
     * Returns a function that removes levels that cannot be valid because single
     * parameter constraints invalidate them.
     */
    @Override
    public Function<? super FactorSpace, ? extends FactorSpace> optimizer() {
      return (FactorSpace factorSpace) -> FactorSpace.create(
          factorSpace.getFactors().stream()
              .map(
                  (Factor factor) -> Factor.create(
                      factor.getName(),
                      factor.getLevels()
                          .stream()
                          .filter(
                              (Object o) -> factorSpace.getConstraints()
                                  .stream()
                                  .filter((Constraint constraint) -> singletonList(factor.getName()).equals(constraint.involvedKeys()))
                                  .allMatch((Constraint constraint) -> constraint.test(new Tuple.Builder().put(factor.getName(), o).build()))
                          )
                          .collect(toList()).toArray()
                  ))
              .collect(toList()),
          factorSpace.getConstraints().stream()
              .filter(
                  (Constraint constraint) -> constraint.involvedKeys().size() > 1
              )
              .collect(toList())
      );
    }

  }
}
