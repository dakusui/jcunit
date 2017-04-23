package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.factorspace.*;
import com.github.dakusui.jcunit8.pipeline.stages.Generator;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Negative;
import com.github.dakusui.jcunit8.pipeline.stages.generators.Passthrough;
import com.github.dakusui.jcunit8.testsuite.SchemafulTupleSet;
import com.github.dakusui.jcunit8.testsuite.TestSuite;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Stream.concat;

/**
 * A pipeline object.
 */
public interface Pipeline<T> {
  TestSuite<T> execute(Config<T> config, ParameterSpace parameterSpace);

  class Standard<T> implements Pipeline<T> {
    @Override
    public TestSuite<T> execute(Config<T> config, ParameterSpace parameterSpace) {
      return generateTestSuite(config, preprocess(config, parameterSpace));
    }

    public TestSuite<T> generateTestSuite(Config<T> config, ParameterSpace parameterSpace) {
      List<Tuple> regularTestTuples = engine(config, parameterSpace);
      TestSuite.Builder<T> builder = new TestSuite.Builder<>(parameterSpace, config.concretizer());
      builder.addAllToRegularTuples(regularTestTuples);
      if (config.getRequirement().generateNegativeTests())
        builder.addAllToNegativeTuples(
            negativeTestGenerator(
                config.getRequirement().generateNegativeTests(),
                toFactorSpaceForNegativeTestGeneration(parameterSpace),
                regularTestTuples,
                config.getRequirement()
            ).generate()
        );
      return builder.build();
    }

    public ParameterSpace preprocess(Config<T> config, ParameterSpace parameterSpace) {
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

    public SchemafulTupleSet engine(Config<T> config, ParameterSpace parameterSpace) {
      return config
          .partitioner().apply(
              config.encoder().apply(
                  parameterSpace
              )
          ).stream()
          .map(config.optimizer())
          .filter((Predicate<FactorSpace>) factorSpace -> !factorSpace.getFactors().isEmpty())
          .map(config.generator(config.getRequirement()))
          .reduce(config.joiner())
          .map(
              (SchemafulTupleSet tuples) -> SchemafulTupleSet.fromTuples(
                  tuples.stream()
                      .map((Tuple tuple) -> {
                        Tuple.Builder builder = new Tuple.Builder();
                        for (String parameterName : parameterSpace.getParameterNames()) {
                          builder.put(parameterName, parameterSpace.getParameter(parameterName).composeValueFrom(tuple));
                        }
                        return builder.build();
                      })
                      .collect(toList())
              )
          )
          .orElseThrow(FrameworkException::unexpectedByDesign);
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
                    parameter.getKnownValues().stream().toArray()
                );
              })
              .collect(toList()),
          concat(
              parameterSpace.getConstraints().stream(),
              parameterSpace.getParameterNames().stream()
                  .map((String name) ->
                      Parameter.Simple.createConstraintFrom(parameterSpace.getParameter(name))
                  )
                  .collect(toList()).stream()
          ).collect(toList())
      );
    }

    private Generator.Base negativeTestGenerator(boolean generateNegativeTests, FactorSpace factorSpace, List<Tuple> tuplesForRegularTests, Requirement requirement) {
      return generateNegativeTests ?
          new Negative(tuplesForRegularTests, factorSpace, requirement) :
          new Passthrough(tuplesForRegularTests, factorSpace, requirement);
    }

    private Parameter toSimpleParameterIfNecessary(Config<T> config, Parameter parameter, List<Constraint> constraints) {
      if (!(parameter instanceof Parameter.Simple) && isInvolvedByAnyConstraint(parameter, constraints)) {
        //noinspection unchecked,RedundantTypeArguments
        return Parameter.Simple.Factory.of(
            Utils.unique(
                Stream.<Object>concat(
                    parameter.getKnownValues().stream(),
                    engine(config, new ParameterSpace.Builder().addParameter(parameter).build()).stream()
                        .map(tuple -> tuple.get(parameter.getName())) // Extraction
                ).collect(toList())
            ))
            .<Parameter.Factory<Object>>setCheck(parameter::check)
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

    public static <T> Pipeline<T> create() {
      return new Standard<>();
    }
  }
}
