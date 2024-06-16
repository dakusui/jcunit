package com.github.jcunit.pipeline;

import com.github.jcunit.annotations.ConfigurePipelineWith;
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
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.jcunit.pipeline.Pipeline.Standard.ConfigArgumentsParser.Keyword.*;
import static com.github.valid8j.classic.Requires.requireNonNull;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

/**
 * A pipeline object.
 */
public interface Pipeline {

  TestSuite execute(ParameterSpace parameterSpace);

  @ParseConfigArgumentsWith(Standard.ConfigArgumentsParser.class)
  class Standard implements Pipeline {
    public static class ConfigArgumentsParser implements ConfigurePipelineWith.PipelineConfigArgumentsParser {
      enum Keyword {
        STRENGTH("strength"),
        NEGATIVE_TEST_GENERATION("negativeTestGeneration"),
        SEED_GENERATOR_METHOD("seedGeneratorMethod");

        private final String keyword;

        Keyword(String keyword) {
          this.keyword = keyword;
        }
      }

      @Override
      public PipelineConfig parseConfig(ConfigurePipelineWith.Entry[] configEntries, Map<String, Supplier<List<Tuple>>> seedGenerators) {
        /*
          Entry[] pipelineArguments() default {
            @Entry(name = "strength", value = "2"),
            @Entry(name = "negativeTestGeneration", value = "true"),
            @Entry(name = "seedGeneratorMethod", value = {})
          };
         */

        validateConfigEntries(configEntries);
        int strength = findConfigEntryValueByName(STRENGTH.keyword, configEntries)
            .map((String[] v) -> v[0])
            .map(Integer::parseInt)
            .orElseThrow(AssertionError::new);
        boolean negativeTestsEnabled = findConfigEntryValueByName(NEGATIVE_TEST_GENERATION.keyword, configEntries)
            .map((String[] v) -> v[0])
            .map(Boolean::parseBoolean)
            .orElseThrow(AssertionError::new);
        List<Tuple> seeds = Arrays.stream(configEntries)
                                  .filter(e -> Objects.equals(e.name(), SEED_GENERATOR_METHOD.keyword))
                                  .flatMap(e -> Arrays.stream(e.value())
                                                      .peek(System.out::println)
                                                      .flatMap(g -> seedGenerators.get(g)
                                                                                  .get()
                                                                                  .stream()))
                                  .collect(toList());
        return new PipelineConfig() {
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
            return seeds;
          }
        };
      }

      void validateConfigEntries(ConfigurePipelineWith.Entry[] configEntries) {
        Set<String> knownParameterNames = knownParameterNames();
        Set<String> unknowns = new LinkedHashSet<>();
        for (ConfigurePipelineWith.Entry v : configEntries) {
          if (!knownParameterNames.contains(v.name()))
            unknowns.add(v.name());
        }
        if (!unknowns.isEmpty())
          throw new RuntimeException(String.format("Unknown parameter names are found in @%s. Check the inheritance hierarchy, too: %s",
                                                   unknowns,
                                                   ConfigurePipelineWith.class.getSimpleName()));
      }

      Set<String> knownParameterNames() {
        return Arrays.stream(Keyword.values())
                     .map(k -> k.keyword)
                     .collect(Collectors.toSet());
      }

      private static Optional<String[]> findConfigEntryValueByName(String name, ConfigurePipelineWith.Entry[] requirement) {
        return Arrays.stream(requirement)
                     .filter((ConfigurePipelineWith.Entry e) -> Objects.equals(e.name(), name))
                     .findFirst()
                     .map(ConfigurePipelineWith.Entry::value);
      }
    }

    private final PipelineSpec config;

    public Standard(PipelineSpec config) {
      this.config = requireNonNull(config);
    }

    @Override
    public TestSuite execute(ParameterSpace parameterSpace) {
      return generateTestSuite(preprocess(parameterSpace));
    }

    public TestSuite generateTestSuite(ParameterSpace parameterSpace) {
      PipelineConfig pipelineConfig = config.getConfig();
      validateSeedTuplesHaveAllParametersAndTheyDontHaveUnknownParameters(pipelineConfig.seeds(), parameterSpace);
      TestSuite.Builder<?> builder = new TestSuite.Builder<>(parameterSpace);
      builder = builder.addAllToSeedTuples(pipelineConfig.seeds());
      List<Tuple> regularTestDataTuples = engine(parameterSpace);
      builder = builder.addAllToRegularTuples(regularTestDataTuples);
      if (pipelineConfig.generateNegativeTests())
        builder = builder.addAllToNegativeTuples(negativeTestGenerator(pipelineConfig.generateNegativeTests(),
                                                                       toFactorSpaceForNegativeTestGeneration(parameterSpace),
                                                                       regularTestDataTuples,
                                                                       pipelineConfig.seeds(),
                                                                       pipelineConfig)
                                                     .generate());
      return builder.build();
    }

    private static void validateSeedTuplesHaveAllParametersAndTheyDontHaveUnknownParameters(List<Tuple> seeds,
                                                                                            ParameterSpace parameterSpace) {
      List<Function<Tuple, String>> checks = asList(
          (Tuple tuple) -> !new HashSet<>(parameterSpace.getParameterNames()).containsAll(tuple.keySet())
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

    private Generator negativeTestGenerator(boolean generateNegativeTests, FactorSpace factorSpace, List<Tuple> tuplesForRegularTests, List<Tuple> encodedSeeds, PipelineConfig pipelineConfig) {
      return generateNegativeTests ?
             new Negative(tuplesForRegularTests, encodedSeeds, factorSpace, pipelineConfig) :
             new Passthrough(tuplesForRegularTests, factorSpace, pipelineConfig);
    }

    private Parameter<?> toSimpleParameterIfNecessary(PipelineSpec config, Parameter<?> parameter, List<Constraint> constraints) {
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

    public static Pipeline create(PipelineSpec config) {
      return new Standard(config);
    }
  }
}
