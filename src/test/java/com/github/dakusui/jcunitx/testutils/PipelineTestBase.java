package com.github.dakusui.jcunitx.testutils;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.*;
import com.github.dakusui.jcunitx.metamodel.Parameter;
import com.github.dakusui.jcunitx.metamodel.ParameterSpace;
import com.github.dakusui.jcunitx.metamodel.parameters.SimpleParameter;
import com.github.dakusui.jcunitx.pipeline.Config;
import com.github.dakusui.jcunitx.pipeline.Pipeline;
import com.github.dakusui.jcunitx.pipeline.Requirement;
import com.github.dakusui.jcunitx.testsuite.SchemafulAArraySet;
import com.github.dakusui.jcunitx.testsuite.TestSuite;

import java.util.Collections;
import java.util.List;

import static java.util.Arrays.asList;

public abstract class PipelineTestBase {

  protected TestSuite generateTestSuite(Parameter<?>... parameters) {
    ParameterSpace parameterSpace = new ParameterSpace.Builder()
        .addAllParameters(asList(parameters))
        .build();
    return new Pipeline.Standard().generateTestSuite(buildConfig(), parameterSpace, null);
  }

  protected TestSuite generateTestSuite(List<Parameter<?>> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard().generateTestSuite(buildConfig(), preprocess(parameters, constraints), null);
  }

  protected ParameterSpace preprocess(Parameter<?>... parameters) {
    return new Pipeline.Standard().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(asList(parameters)).build());
  }

  protected ParameterSpace preprocess(List<Parameter<?>> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(parameters).addAllConstraints(constraints).build());
  }

  protected SchemafulAArraySet engine(List<Parameter<?>> parameters, List<Constraint> constraints) {
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

  protected Parameter.Descriptor<CustomParameter.ValuePair> customParameterFactory() {
    return new Parameter.Descriptor.Base<CustomParameter.ValuePair>() {
      @Override
      public Parameter<CustomParameter.ValuePair> create(String name) {
        return new CustomParameter(name, asList("hello", "world", "everyone"));
      }
    };
  }

  protected Parameter.Descriptor<String> simpleParameterFactory(String... values) {
    return SimpleParameter.Descriptor.of(asList(values));
  }

  protected Parameter.Descriptor<String> simpleParameterFactoryWithDefaultValues() {
    return simpleParameterFactory("default", "values");
  }

  private Config buildConfig() {
    return Config.Builder.forTuple(
        requirement()
    ).build();
  }


  protected Requirement requirement() {
    return new Requirement.Builder()
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
            Constraint.create("alwaysTrue[simple1]", (AArray tuple) -> false, "simple1") // Never becomes true
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
