package com.github.dakusui.jcunit8.ututiles;


import com.github.dakusui.jcunit8.testutils.TestBase;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.*;
import com.github.jcunit.pipeline.Config;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.pipeline.SchemafulTupleSet;
import com.github.jcunit.testsuite.TestSuite;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public abstract class PipelineTestBase extends TestBase {

  protected TestSuite generateTestSuite(Parameter<?>... parameters) {
    ParameterSpace parameterSpace = new ParameterSpace.Builder()
        .addAllParameters(asList(parameters))
        .build();
    return new Pipeline.Standard().generateTestSuite(buildConfig(), parameterSpace);
  }

  protected TestSuite generateTestSuite(List<Parameter<?>> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard().generateTestSuite(buildConfig(), preprocess(parameters, constraints));
  }

  protected ParameterSpace preprocess(Parameter<?>... parameters) {
    return new Pipeline.Standard().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(asList(parameters)).build());
  }

  protected ParameterSpace preprocess(List<Parameter<?>> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(parameters).addAllConstraints(constraints).build());
  }

  protected SchemafulTupleSet engine(List<Parameter<?>> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard().engine(buildConfig(), new ParameterSpace.Builder().addAllParameters(parameters).addAllConstraints(constraints).build());
  }

  protected FactorSpace encode(List<Parameter<?>> parameters, List<Constraint> constraints) {
    return buildConfig()
        .encoder().apply(
            new ParameterSpace.Builder()
                .addAllParameters(parameters)
                .addAllConstraints(constraints)
                .build()
        );
  }

  protected List<FactorSpace> partition(FactorSpace input) {
    return buildConfig().partitioner().apply(input);
  }

  protected Parameter.Factory<CustomParameter.ValuePair> customParameterFactory() {
    return new Parameter.Factory.Base<CustomParameter.ValuePair>() {
      @Override
      public Parameter<CustomParameter.ValuePair> create(String name) {
        return new CustomParameter(name, asList("hello", "world", "everyone"));
      }
    };
  }

  protected Parameter.Factory<String> simpleParameterFactory(String... values) {
    return Parameter.Simple.Factory.of(asList(values));
  }

  protected Parameter.Factory<String> simpleParameterFactoryWithDefaultValues() {
    return simpleParameterFactory("default", "values");
  }

  private Config buildConfig() {
    return Config.Builder.forTuple(
        requirement()
    ).build();
  }


  protected Requirement requirement() {
    return new Requirement.Builder()
        .withStrength(2)
        .withNegativeTestGeneration(false)
        .build();
  }

  protected FactorSpace buildSimpleFactorSpaceWithImpossibleConstraint() {
    return FactorSpace.create(
        asList(
            Factor.create("simple1", new Object[] { "default", "value" }),
            Factor.create("simple2", new Object[] { "default", "value" }),
            Factor.create("simple3", new Object[] { "default", "value" })
        ),
        Collections.singletonList(
            Constraint.create("alwaysTrue[simple1]", (Tuple tuple) -> false, "simple1") // Never becomes true
        )
    );
  }

  protected FactorSpace buildSimpleFactorSpaceWithoutConstraint() {
    return FactorSpace.create(
        asList(
            Factor.create("simple1", new Object[] { "default", "value" }),
            Factor.create("simple2", new Object[] { "default", "value" }),
            Factor.create("simple3", new Object[] { "default", "value" })
        ),
        Collections.emptyList(
        )
    );
  }
}
