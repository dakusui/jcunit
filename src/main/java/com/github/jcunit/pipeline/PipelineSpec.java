package com.github.jcunit.pipeline;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Factor;
import com.github.jcunit.factorspace.FactorSpace;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.stages.Encoder;
import com.github.jcunit.pipeline.stages.Generator;
import com.github.jcunit.pipeline.stages.Joiner;
import com.github.jcunit.pipeline.stages.Partitioner;

import java.util.List;
import java.util.function.BinaryOperator;
import java.util.function.Function;

import static java.util.Collections.singletonList;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.toList;

public interface PipelineSpec {
  PipelineConfig getConfig();

  /**
   * Returns a function that encodes a parameter space into internal factor spaces.
   *
   * @return A function that encodes a parameter space.
   */
  Function<ParameterSpace, FactorSpace> encoder();

  Function<FactorSpace, List<FactorSpace>> partitioner();

  Function<FactorSpace, SchemafulTupleSet> generator(ParameterSpace parameterSpace);

  BinaryOperator<SchemafulTupleSet> joiner();

  Function<? super FactorSpace, ? extends FactorSpace> optimizer();

  class Builder {
    private final PipelineConfig pipelineConfig;
    private Generator.Factory generatorFactory;
    private Joiner joiner;
    private Partitioner partitioner;

    public Builder(PipelineConfig pipelineConfig) {
      this.pipelineConfig = pipelineConfig;
      this.withJoiner(new Joiner.Standard(pipelineConfig))
          .withPartitioner(new Partitioner.Standard(pipelineConfig))
          .withGeneratorFactory(new Generator.Factory.Standard(pipelineConfig));
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

    public PipelineSpec build() {
      return new Impl(pipelineConfig, generatorFactory, joiner, partitioner);
    }
  }

  class Impl implements PipelineSpec {
    private final Generator.Factory generatorFactory;
    private final Joiner joiner;
    private final Partitioner partitioner;
    private final PipelineConfig pipelineConfig;
    private final Encoder encoder;

    public Impl(PipelineConfig pipelineConfig, Generator.Factory generatorFactory, Joiner joiner, Partitioner partitioner) {
      this.generatorFactory = requireNonNull(generatorFactory);
      this.encoder = new Encoder.Standard();
      this.joiner = requireNonNull(joiner);
      this.partitioner = requireNonNull(partitioner);
      this.pipelineConfig = requireNonNull(pipelineConfig);
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
    public Function<FactorSpace, SchemafulTupleSet> generator(ParameterSpace parameterSpace) {
      return (FactorSpace factorSpace) -> new SchemafulTupleSet.Builder(factorSpace.getFactors()
                                                                                   .stream()
                                                                                   .map(Factor::getName)
                                                                                   .collect(toList()))
          .addAll(generatorFactory.create(factorSpace,
                                          ParameterSpace.encodeSeedTuples(parameterSpace,
                                                                          this.getConfig().seeds()))
                                  .generate())
          .build();
    }

    @Override
    public BinaryOperator<SchemafulTupleSet> joiner() {
      return joiner;
    }

    @Override
    public PipelineConfig getConfig() {
      return pipelineConfig;
    }

    /**
     * Returns a function that removes levels that cannot be valid because single
     * parameter constraints invalidate them.
     */
    @Override
    public Function<? super FactorSpace, ? extends FactorSpace> optimizer() {
      return (FactorSpace factorSpace)
          -> FactorSpace.create(factorSpace.getFactors()
                                           .stream()
                                           .map((Factor factor) -> Factor.create(
                                               factor.getName(),
                                               factor.getLevels()
                                                     .stream()
                                                     .filter((Object o) -> factorSpace.getConstraints()
                                                                                      .stream()
                                                                                      .filter((Constraint constraint) -> singletonList(factor.getName()).equals(constraint.involvedKeys()))
                                                                                      .allMatch((Constraint constraint) -> constraint.test(new Tuple.Builder().put(factor.getName(), o)
                                                                                                                                                              .build())))
                                                     .collect(toList())
                                                     .toArray()))
                                           .collect(toList()),
                                factorSpace.getConstraints()
                                           .stream()
                                           .filter((Constraint constraint) -> constraint.involvedKeys().size() > 1)
                                           .collect(toList())
      );
    }
  }
}