package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.*;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Negative;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Passthrough;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestScenario;
import com.github.dakusui.jcunit8.testsuite.TestSuite;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * A pipeline object.
 */
@SuppressWarnings("unchecked")
public interface Pipeline {
  TestSuite execute(Config config, ParameterSpace parameterSpace, TestScenario testScenarioFactory);

  class Standard implements Pipeline {
    @Override
    public TestSuite execute(Config config, ParameterSpace parameterSpace, TestScenario testScenario) {
      return generateTestSuite(config, preprocess(config, parameterSpace), testScenario);
    }

    public TestSuite generateTestSuite(Config config, ParameterSpace parameterSpace, TestScenario testScenario) {
      validateSeeds(config.getRequirement().seeds(), parameterSpace);
      TestSuite.Builder builder = new TestSuite.Builder(parameterSpace, testScenario);
      builder = builder.addAllToSeedTuples(config.getRequirement().seeds());
      List<Tuple> regularTestTuples = engine(config, parameterSpace);
      builder = builder.addAllToRegularTuples(regularTestTuples);
      if (config.getRequirement().generateNegativeTests())
        builder = builder.addAllToNegativeTuples(
            negativeTestGenerator(
                config.getRequirement().generateNegativeTests(),
                toFactorSpaceForNegativeTestGeneration(parameterSpace),
                regularTestTuples,
                config.getRequirement().seeds(),
                config.getRequirement()
            ).generate()
        );
      return builder.build();
    }

    private void validateSeeds(List<Tuple> seeds, ParameterSpace parameterSpace) {
      List<Function<Tuple, String>> checks = asList(
          (Tuple tuple) -> !parameterSpace.getParameterNames().containsAll(tuple.keySet()) ?
              String.format("Unknown parameter(s) were found: %s in tuple: %s",
                  new LinkedList<String>() {{
                    addAll(tuple.keySet());
                    removeAll(parameterSpace.getParameterNames());
                  }},
                  tuple
              ) :
              null,
          (Tuple tuple) -> !tuple.keySet().containsAll(parameterSpace.getParameterNames()) ?
              String.format("Parameter(s) were not found: %s in tuple: %s",
                  new LinkedList<String>() {{
                    addAll(parameterSpace.getParameterNames());
                    removeAll(tuple.keySet());
                  }},
                  tuple
              ) :
              null
      );
      List<String> errors = seeds.stream(
      ).flatMap(
          seed -> checks.stream(
          ).map(each -> each.apply(seed))
      ).filter(
          Objects::nonNull
      ).collect(toList());
      if (!errors.isEmpty())
        throw new InvalidTestException(
            String.format(
                "Error(s) are found in seeds: %s",
                errors
            ));
    }

    public ParameterSpace preprocess(Config config, ParameterSpace parameterSpace) {
      return new ParameterSpace.Builder()
          .addAllParameters(
              parameterSpace.getParameterNames().stream()
                  .map((String parameterName) -> toSimpleParameterIfNecessary(
                      config,
                      parameterSpace.getParameter(parameterName),
                      parameterSpace.getConstraints()
                  ))
                  .collect(toList()))
          .addAllConstraints(parameterSpace.getConstraints())
          .build();
    }

    public SchemafulTupleSet engine(Config config, ParameterSpace parameterSpace) {
      return config.partitioner().apply(
          config.encoder().apply(
              parameterSpace
          )
      ).stream()
          .map(config.optimizer())
          .filter((Predicate<FactorSpace>) factorSpace -> !factorSpace.getFactors().isEmpty())
          .map(config.generator(parameterSpace, config.getRequirement()))
          .reduce(config.joiner())
          .map(
              (SchemafulTupleSet tuples) -> new SchemafulTupleSet.Builder(parameterSpace.getParameterNames()).addAll(
                  tuples.stream()
                      .map((Tuple tuple) -> {
                        Tuple.Builder builder = new Tuple.Builder();
                        for (String parameterName : parameterSpace.getParameterNames()) {
                          builder.put(parameterName, parameterSpace.getParameter(parameterName).composeValue(tuple));
                        }
                        return builder.build();
                      })
                      .collect(toList())
              ).build()
          )
          .orElseThrow(TestDefinitionException::noParameterFound);
    }


    /**
     * This method should be used for a parameter space that does not contain a
     * constraint involving a non-simple parameter.
     */
    private FactorSpace toFactorSpaceForNegativeTestGeneration(ParameterSpace parameterSpace) {
      PipelineException.checkIfNoNonSimpleParameterIsInvolvedByAnyConstraint(parameterSpace);
      return FactorSpace.create(
          parameterSpace.getParameterNames().stream()
              .map((String s) -> {
                Parameter<Object> parameter = parameterSpace.getParameter(s);
                return Factor.create(
                    s,
                    parameter.getKnownValues().toArray()
                );
              })
              .collect(toList()),
          new ArrayList<>(parameterSpace.getConstraints())
      );
    }

    private Generator negativeTestGenerator(boolean generateNegativeTests, FactorSpace factorSpace, List<Tuple> tuplesForRegularTests, List<Tuple> encodedSeeds, Requirement requirement) {
      return generateNegativeTests ?
          new Negative(tuplesForRegularTests, encodedSeeds, factorSpace, requirement) :
          new Passthrough(tuplesForRegularTests, factorSpace, requirement);
    }

    @SuppressWarnings("unchecked")
    private Parameter toSimpleParameterIfNecessary(Config config, Parameter parameter, List<Constraint> constraints) {
      if (!(parameter instanceof Parameter.Simple) && isInvolvedByAnyConstraint(parameter, constraints)) {
        return Parameter.Simple.Factory.of(
            Utils.unique(
                Stream.<Object>concat(
                    parameter.getKnownValues().stream(),
                    engine(config, new ParameterSpace.Builder().addParameter(parameter).build()).stream()
                        .map(tuple -> tuple.get(parameter.getName())) // Extraction
                ).collect(toList())
            ))
            .create(parameter.getName());
      }
      return parameter;
    }

    /**
     * Checks is a parameter is referenced by any constraint in a given list or it
     * has any known actual values.
     * <p>
     * <p>
     * The reason why we check if it has known levels at the same time is like this.
     * We can state that a conventional constraint requires some combinations are
     * absent from the final test suite. On the other hans, a 'known level' requires
     * it to be presented in a final test suite (seeds). Then we can state that
     * known levels are 'presence constraints' while conventional ones are 'absence
     * constraints'.
     *
     * @param parameter   A parameter to be checked.
     * @param constraints A list of constraints to be checked with the {@code parameter}.
     */
    private boolean isInvolvedByAnyConstraint(Parameter<?> parameter, List<Constraint> constraints) {
      return isReferencedBy(parameter, constraints) || !parameter.getKnownValues().isEmpty();
    }

    private boolean isReferencedBy(Parameter parameter, List<Constraint> constraints) {
      return constraints.stream().anyMatch(each -> each.involvedKeys().contains(parameter.getName()));
    }

    public static Pipeline create() {
      return new Standard();
    }
  }
}
