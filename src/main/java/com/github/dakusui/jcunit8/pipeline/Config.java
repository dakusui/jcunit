package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.stages.Encoder;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.pipeline.stages.Joiner;
import com.github.dakusui.jcunit8.pipeline.stages.Partitioner;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface Config {
  Requirement getRequirement();

  /**
   * Returns a function that encodes a parameter space into internal factor spaces.
   * @return A function that encodes a parameter space.
   */
  Function<ParameterSpace, FactorSpace> encoder();

  Function<FactorSpace, List<FactorSpace>> partitioner();

  Function<FactorSpace, SchemafulTupleSet> generator(ParameterSpace parameterSpace, Requirement requirement);

  BinaryOperator<SchemafulTupleSet> joiner();

  Function<? super FactorSpace, ? extends FactorSpace> optimizer();

  class Builder {
    private final Requirement       requirement;
    private       Generator.Factory generatorFactory;
    private       Joiner            joiner;
    private       Partitioner       partitioner;

    public static Builder forTuple(Requirement requirement) {
      return new Builder(requirement);
    }

    public Builder(Requirement requirement) {
      this.requirement = requirement;
      this.withGeneratorFactory(new Generator.Factory.Standard());
      this.withJoiner(new Joiner.Standard(requirement));
      this.withPartitioner(new Partitioner.Standard(requirement));
    }

    public Builder withGeneratorFactory(Generator.Factory generatorFactory) {
      this.generatorFactory = generatorFactory;
      return this;
    }

    public Builder withJoiner(Joiner joiner) {
      this.joiner = joiner;
      return this;
    }

    public Builder withPartitioner(Partitioner partitioner) {
      this.partitioner = partitioner;
      return this;
    }

    public Config build() {
      return new Impl(requirement, generatorFactory, joiner, partitioner);
    }
  }

  class Impl implements Config {
    private final Generator.Factory generatorFactory;
    private final Joiner            joiner;
    private final Partitioner       partitioner;
    private final Requirement       requirement;
    private final Encoder           encoder;

    public Impl(Requirement requirement, Generator.Factory generatorFactory, Joiner joiner, Partitioner partitioner) {
      this.generatorFactory = requireNonNull(generatorFactory);
      this.encoder = new Encoder.Standard();
      this.joiner = requireNonNull(joiner);
      this.partitioner = requireNonNull(partitioner);
      this.requirement = requireNonNull(requirement);
    }

    @Override
    public Function<ParameterSpace, FactorSpace> encoder() {
      return this.encoder;
    }

    @Override
    public Function<FactorSpace, List<FactorSpace>> partitioner() {
      return partitioner;
    }

    @Override
    public Function<FactorSpace, SchemafulTupleSet> generator(ParameterSpace parameterSpace, Requirement requirement) {
      return (FactorSpace factorSpace) -> new SchemafulTupleSet.Builder(
          factorSpace.getFactors().stream(
          ).map(
              Factor::getName
          ).collect(
              toList()
          )
      ).addAll(
          generatorFactory.create(
              factorSpace,
              requirement,
              ParameterSpace.encodeSeedTuples(parameterSpace, requirement.seeds())
          ).generate()
      ).build();
    }

    @Override
    public BinaryOperator<SchemafulTupleSet> joiner() {
      return joiner;
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
