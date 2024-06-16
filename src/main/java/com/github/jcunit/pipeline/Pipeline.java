package com.github.jcunit.pipeline;

import com.github.jcunit.annotations.ConfigureWith;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.exceptions.Checks;
import com.github.jcunit.exceptions.InvalidTestException;
import com.github.jcunit.exceptions.TestDefinitionException;
import com.github.jcunit.factorspace.*;
import com.github.jcunit.pipeline.stages.Generator;
import com.github.jcunit.pipeline.stages.generators.Negative;
import com.github.jcunit.pipeline.stages.generators.Passthrough;
import com.github.jcunit.testsuite.TestSuite;
import com.github.jcunit.utils.InternalUtils;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * A pipeline object.
 */
public interface Pipeline {

  TestSuite execute(ParameterSpace parameterSpace);

  @ParseRequirementWith(Standard.ReqParser.class)
  class Standard implements Pipeline {
    public static class ReqParser implements ConfigureWith.RequirementParser {
      @Override
      public Requirement parseRequirement(ConfigureWith.Entry[] requirement) {
        /*
          Entry[] pipelineArguments() default {
            @Entry(name = "strength", value = "2"),
            @Entry(name = "negativeTestGeneration", value = "true"),
            @Entry(name = "seedGeneratorMethod", value = "seeds")
          };
         */
        int strength = findRequirementEntryValueByName("strength", requirement)
            .map((String[] v) -> v[0])
            .map(Integer::parseInt)
            .orElseThrow(AssertionError::new);
        boolean negativeTestsEnabled = findRequirementEntryValueByName("negativeTestGeneration", requirement)
            .map((String[] v) -> v[0])
            .map(Boolean::parseBoolean)
            .orElseThrow(AssertionError::new);
        return new Requirement() {
          @Override
          public int strength() {
            return strength;
          }

          @Override
          public boolean generateNegativeTests() {
            return negativeTestsEnabled;
          }

          @Override
          public List<Tuple> seeds() {
            return Collections.emptyList();
          }
        };
      }

      private static Optional<String[]> findRequirementEntryValueByName(String name, ConfigureWith.Entry[] requirement) {
        return Arrays.stream(requirement)
                     .filter((ConfigureWith.Entry e) -> Objects.equals(e.name(), name))
                     .findFirst()
                     .map(ConfigureWith.Entry::value);
      }
    }

    private final PipelineConfig config;

    public Standard(PipelineConfig config) {
      this.config = requireNonNull(config);
    }

    @Override
    public TestSuite execute(ParameterSpace parameterSpace) {
      return generateTestSuite(preprocess(parameterSpace));
    }

    public TestSuite generateTestSuite(ParameterSpace parameterSpace) {
      Requirement requirement = config.getRequirement();
      validateSeedTuplesHaveAllParametersAndTheyDontHaveUnknownParameters(requirement.seeds(), parameterSpace);
      TestSuite.Builder<?> builder = new TestSuite.Builder<>(parameterSpace);
      builder = builder.addAllToSeedTuples(requirement.seeds());
      List<Tuple> regularTestDataTuples = engine(parameterSpace);
      builder = builder.addAllToRegularTuples(regularTestDataTuples);
      if (requirement.generateNegativeTests())
        builder = builder.addAllToNegativeTuples(negativeTestGenerator(requirement.generateNegativeTests(),
                                                                       toFactorSpaceForNegativeTestGeneration(parameterSpace),
                                                                       regularTestDataTuples,
                                                                       requirement.seeds(),
                                                                       requirement)
                                                     .generate());
      return builder.build();
    }

    private static void validateSeedTuplesHaveAllParametersAndTheyDontHaveUnknownParameters(List<Tuple> seeds,
                                                                                            ParameterSpace parameterSpace) {
      List<Function<Tuple, String>> checks = asList(
          (Tuple tuple) -> !parameterSpace.getParameterNames().containsAll(tuple.keySet())
                           ? String.format("Unknown parameter(s) were found: %s in tuple: %s",
                                           new LinkedList<String>() {{
                                             addAll(tuple.keySet());
                                             removeAll(parameterSpace.getParameterNames());
                                           }},
                                           tuple) :
                           null,
          (Tuple tuple) -> !tuple.keySet().containsAll(parameterSpace.getParameterNames()) ?
                           String.format("Parameter(s) were not found: %s in tuple: %s",
                                         new LinkedList<String>() {{
                                           addAll(parameterSpace.getParameterNames());
                                           removeAll(tuple.keySet());
                                         }},
                                         tuple) :
                           null);
      List<String> errors = seeds.stream()
                                 .flatMap(seed -> checks.stream()
                                                        .map(each -> each.apply(seed)))
                                 .filter(Objects::nonNull)
                                 .collect(toList());
      if (!errors.isEmpty())
        throw new InvalidTestException(
            String.format(
                "Error(s) are found in seeds: %s",
                errors
            ));
    }

    public ParameterSpace preprocess(ParameterSpace parameterSpace) {
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

    public SchemafulTupleSet engine(ParameterSpace parameterSpace) {
      return config.partitioner().apply(config.encoder().apply(parameterSpace))
                   .stream()
                   .map(config.optimizer())
                   .filter((Predicate<FactorSpace>) factorSpace -> !factorSpace.getFactors().isEmpty())
                   .map(config.generator(parameterSpace))
                   .reduce(config.joiner())
                   .map(
                       (SchemafulTupleSet tuples) -> new SchemafulTupleSet.Builder(parameterSpace.getParameterNames())
                           .addAll(tuples.stream()
                                         .map((Tuple tuple) -> {
                                           Tuple.Builder builder = new Tuple.Builder();
                                           for (String parameterName : parameterSpace.getParameterNames()) {
                                             builder.put(parameterName, parameterSpace.getParameter(parameterName)
                                                                                      .composeValue(tuple));
                                           }
                                           return builder.build();
                                         })
                                         .collect(toList()))
                           .build())
                   .orElseThrow(TestDefinitionException::noParameterFound);
    }


    /**
     * This method should be used for a parameter space that does not contain a
     * constraint involving a non-simple parameter.
     */
    private static FactorSpace toFactorSpaceForNegativeTestGeneration(ParameterSpace parameterSpace) {
      Checks.checkIfNoNonSimpleParameterIsInvolvedByAnyConstraint(parameterSpace);
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

    private Parameter<?> toSimpleParameterIfNecessary(PipelineConfig config, Parameter<?> parameter, List<Constraint> constraints) {
      if (!(parameter instanceof Parameter.Simple) && isInvolvedByAnyConstraint(parameter, constraints)) {
        List<Object> values = Stream.concat(parameter.getKnownValues().stream(),
                                            engine(new ParameterSpace.Builder().addParameter(parameter)
                                                                               .build())
                                                .stream()
                                                .map(tuple -> tuple.get(parameter.getName()))) // Extraction
                                    .collect(toList());
        return Parameter.Simple.Factory.of(InternalUtils.unique(values)).create(parameter.getName());
      }
      return parameter;
    }

    /**
     * Checks is a parameter is referenced by any constraint in a given list, or it
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

    private boolean isReferencedBy(Parameter<?> parameter, List<Constraint> constraints) {
      return constraints.stream().anyMatch(each -> each.involvedKeys().contains(parameter.getName()));
    }

    public static Pipeline create(PipelineConfig config) {
      return new Standard(config);
    }
  }
}
