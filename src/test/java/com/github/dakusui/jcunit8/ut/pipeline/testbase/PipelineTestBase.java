package com.github.dakusui.jcunit8.ut.pipeline.testbase;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.Requirement;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import com.github.dakusui.jcunit8.ut.UTBase;

import java.util.List;

import static java.util.Arrays.asList;

public abstract class PipelineTestBase extends UTBase {

  protected TestSuite<Tuple> generateTestSuite(Parameter... parameters) {
    ParameterSpace parameterSpace = new ParameterSpace.Builder()
        .addAllParameters(asList(parameters))
        .build();
    return new Pipeline.Standard<Tuple>().generateTestSuite(buildConfig(), parameterSpace);
  }

  protected TestSuite<Tuple> generateTestSuite(List<Parameter> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard<Tuple>().generateTestSuite(buildConfig(), preprocess(parameters, constraints));
  }

  protected ParameterSpace preprocess(Parameter... parameters) {
    return new Pipeline.Standard<Tuple>().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(asList(parameters)).build());
  }

  protected ParameterSpace preprocess(List<Parameter> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard<Tuple>().preprocess(buildConfig(), new ParameterSpace.Builder().addAllParameters(parameters).addAllConstraints(constraints).build());
  }

  protected SchemafulTupleSet engine(List<Parameter> parameters, List<Constraint> constraints) {
    return new Pipeline.Standard<Tuple>().engine(buildConfig(), new ParameterSpace.Builder().addAllParameters(parameters).addAllConstraints(constraints).build());
  }

  protected FactorSpace encode(List<Parameter> parameters, List<Constraint> constraints) {
    return buildConfig()
        .encoder().apply(
            new ParameterSpace.Builder()
                .addAllParameters(parameters)
                .addAllConstraints(constraints)
                .build()
        );
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

  private Config<Tuple> buildConfig() {
    return Config.Builder.forTuple(
        new Requirement.Builder()
            .withNegativeTestGeneration(false)
            .build())
        .build();
  }


  protected Requirement requirement() {
    return new Requirement.Builder().build();
  }
}
