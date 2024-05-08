package com.github.jcunit.core.model;

import com.github.jcunit.annotations.DefineParameter;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.jcunit.factorspace.Factor.VOID;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
class Reference<T, R> implements ParameterSpec<Parameter<T>, T, R> {

  private final String name;
  private final Function<String, T> parser;
  private final Function<T, Function<Tuple, R>> resolver;
  private final ParameterSpaceSpec parameterSpaceSpec;
  private final boolean isSeed;

  /**
   * @param name               A name of the parameter which is defined by this object.
   * @param parser             A function that parses strings given to {@link DefineParameter#with()} value into generation time parameter value.
   * @param resolver           A function that converts a generation time parameter value into a corresponding execution time parameter value.
   * @param isSeed
   * @param parameterSpaceSpec
   */
  public Reference(String name,
                   Function<String, T> parser,
                   Function<T, Function<Tuple, R>> resolver,
                   boolean isSeed,
                   ParameterSpaceSpec parameterSpaceSpec) {
    this.resolver = resolver;
    this.name = name;
    this.parser = parser;
    this.parameterSpaceSpec = parameterSpaceSpec;
    this.isSeed = isSeed;
  }

  public static <T, R> Reference<T, R> create(String name,
                                              List<String> parameterNames,
                                              Function<String, T> parser,
                                              Function<T, Function<Tuple, R>> resolver,
                                              Function<String, Function<Object, Set<String>>> references,
                                              Function<String, List<Object>> possibleValues) {
    return createReference(name, parser, resolver, createParameterSpaceSpec(parameterNames, references, possibleValues));
  }

  private static <T, R> Reference<T, R> createReference(String name, Function<String, T> parser, Function<T, Function<Tuple, R>> resolver, ParameterSpaceSpec parameterSpaceSpec1) {
    return new Reference<>(name,
                           parser,
                           resolver,
                           isSeed(parameterSpaceSpec1, name, parameterSpaceSpec1.parameterNames().toArray(new String[0])),
                           parameterSpaceSpec1);
  }

  private static ParameterSpaceSpec createParameterSpaceSpec(List<String> parameterNames, Function<String, Function<Object, Set<String>>> references, Function<String, List<Object>> possibleValues) {
    return new ParameterSpaceSpec() {
      @Override
      public List<String> parameterNames() {
        return parameterNames;
      }

      @Override
      public Function<Object, Set<String>> referencesFor(String parameterName) {
        return references.apply(parameterName);
      }

      @SuppressWarnings("unchecked")
      @Override
      public <TT> List<TT> possibleValidValuesFor(String parameterName) {
        return (List<TT>) possibleValues.apply(parameterName);
      }
    };
  }

  @Override
  public GenerationTimeParameterFactory<Parameter<T>, T> parameterFactory() {
    return createGenerationTimeParameterFactory(name, parser, isSeed, this.parameterSpaceSpec);
  }

  private static <T> GenerationTimeParameterFactory<Parameter<T>, T> createGenerationTimeParameterFactory(final String name, final Function<String, T> parser, final boolean isSeed, final ParameterSpaceSpec parameterSpaceSpec) {
    return new GenerationTimeParameterFactory<Parameter<T>, T>() {
      @Override
      public Parameter<T> createParameter(String... args) {
        return Reference.createParameter(isSeed, name, parser, args);
      }

      /**
       * @return Created constraints
       */
      @Override
      public List<Constraint> createConstraints() {
        return Reference.createConstraints(isSeed, parameterSpaceSpec, name);
      }
    };
  }

  private static List<Constraint> createConstraints(boolean isSeed, ParameterSpaceSpec parameterSpaceSpec, String parameterName) {
    return isSeed ? emptyList()
                  : Reference.nonSeedAttributeMustBeReferencedAtLeastOnce(parameterSpaceSpec, parameterName)
                             .map(Collections::singletonList)
                             .orElse(emptyList());
  }

  private static <T> Parameter<T> createParameter(boolean isSeed, String parameterName, Function<String, T> parser, String[] args) {
    return new Parameter.Simple.Impl<>(!isSeed, parameterName, Arrays.stream(args)
                                                            .map(parser)
                                                            .collect(toList()));
  }

  private static boolean isSeed(ParameterSpaceSpec parameterSpaceSpec, String attribute, String[] attributeNames) {
    Set<String> referencingAttributes = new HashSet<>();
    for (String each : attributeNames) {
      if (Objects.equals(each, attribute))
        continue;
      if (areAllPossibleValuesReferencing(parameterSpaceSpec, attribute, each))
        return true;
      if (anyPossibleValueReferencing(parameterSpaceSpec, attribute, each))
        referencingAttributes.add(each);
    }
    return referencingAttributes.isEmpty();
  }

  private static boolean areAllPossibleValuesReferencing(ParameterSpaceSpec parameterSpaceSpec, String referencingAttribute, String referencedAttribute) {
    return parameterSpaceSpec.possibleValidValuesFor(referencingAttribute)
                             .stream()
                             .allMatch(eachPossibleValue -> parameterSpaceSpec.referencesFor(referencingAttribute)
                                                                              .apply(eachPossibleValue)
                                                                              .contains(referencedAttribute));
  }

  private static boolean anyPossibleValueReferencing(ParameterSpaceSpec parameterSpaceSpec, String referencingAttribute, String referencedAttribute) {
    return parameterSpaceSpec.possibleValidValuesFor(referencingAttribute)
                             .stream()
                             .anyMatch(eachPossibleValue -> parameterSpaceSpec.referencesFor(referencingAttribute)
                                                                              .apply(eachPossibleValue)
                                                                              .contains(referencedAttribute));
  }

  private static Optional<Constraint> nonSeedAttributeMustBeReferencedAtLeastOnce(ParameterSpaceSpec parameterSpaceSpec, String referencedAttribute) {
    if (parameterSpaceSpec.parameterNames().isEmpty()) {
      return Optional.empty();
    }
    String[] conditionallyReferencingAttributes = parameterSpaceSpec.parameterNames()
                                                                    .stream()
                                                                    .filter(each -> !Objects.equals(referencedAttribute, each))
                                                                    .filter(each -> anyPossibleValueReferencing(parameterSpaceSpec, each, referencedAttribute))
                                                                    .toArray(String[]::new);
    return Optional.of(
        Constraint.create("nonSeedAttributeMustBeReferencedAtLeastOnce:" + referencedAttribute,
                          tuple -> tuple.get(referencedAttribute).equals(VOID) != referencedByAnyAttribute(tuple,
                                                                                                           referencedAttribute,
                                                                                                           conditionallyReferencingAttributes,
                                                                                                           parameterSpaceSpec),
                          Stream.concat(
                                    Stream.of(referencedAttribute),
                                    Arrays.stream(conditionallyReferencingAttributes))
                                .toArray(String[]::new)));
  }

  private static boolean referencedByAnyAttribute(Tuple tuple, String referencedAttribute, String[] conditionallyReferencingAttributes, ParameterSpaceSpec parameterSpaceSpec) {
    for (String eachReferencingAttribute : conditionallyReferencingAttributes) {
      if (parameterSpaceSpec.referencesFor(eachReferencingAttribute)
                            .apply(tuple.get(eachReferencingAttribute))
                            .contains(referencedAttribute))
        return true;
    }
    return false;
  }

  @Override
  public ExecutionTimeValueResolver<T, R> valueResolver() {
    return (generationTimeValue, testData) -> resolver.apply(generationTimeValue).apply(testData);
  }

  @Override
  public String name() {
    return name;
  }
}
