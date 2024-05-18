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
 * @param <G> Generation-time parameter type
 * @param <E> Execution-time parameter type
 * // @formatter:on
 */
class ParameterFactoryImpl<G , E> implements ParameterFactory<Parameter<G>, G, E> {
  private final String name;
  private final Function<String, G> parser;
  private final Function<G, Function<Tuple, E>> resolver;
  private final ParameterSpaceSpec parameterSpaceSpec;
  private final boolean isSeed;

  /**
   * @param name               A name of the parameter which is defined by this object.
   * @param parser             A function that parses strings given to {@link DefineParameter#with()} value into generation
   *                           time parameter value.
   * @param resolver           A function that converts a generation time parameter value into a corresponding execution
   *                           time parameter value.
   * @param isSeed             A `boolean` value which tells if the instance is a "seed" or not.
   * @param parameterSpaceSpec An instance that defines the parameter space to which the new instance belongs.
   */
  public ParameterFactoryImpl(String name,
                              Function<String, G> parser,
                              Function<G, Function<Tuple, E>> resolver,
                              boolean isSeed,
                              ParameterSpaceSpec parameterSpaceSpec) {
    this.resolver = resolver;
    this.name = name;
    this.parser = parser;
    this.parameterSpaceSpec = parameterSpaceSpec;
    this.isSeed = isSeed;
  }

  @Override
  public Parameter<G> createParameter(String[] args) {
    return new Parameter.Simple.Impl<>(!this.isSeed, this.name, Arrays.stream(args)
                                                                      .map(this.parser)
                                                                      .collect(toList()));
  }

  @Override
  public List<Constraint> createConstraint() {
    return ParameterFactoryImpl.createConstraints(this.isSeed, this.parameterSpaceSpec, this.name);
  }

  @Override
  public ExecutionTimeValueResolver<G, E> valueResolver() {
    return (generationTimeValue, testData) -> resolver.apply(generationTimeValue).apply(testData);
  }

  @Override
  public String name() {
    return name;
  }

  public static <T, R> ParameterFactory<Parameter<T>, T, R> create(String name,
                                                                   List<String> parameterNames,
                                                                   Function<String, T> parser,
                                                                   Function<T, Function<Tuple, R>> resolver,
                                                                   Function<String, Function<Object, Set<String>>> references,
                                                                   Function<String, List<Object>> possibleValues) {
    return create(name, parser, resolver, ParameterSpaceSpec.createParameterSpaceSpec(parameterNames, references, possibleValues));
  }

  public static <T, R> ParameterFactory<Parameter<T>, T, R> create(String name, Function<String, T> parser, Function<T, Function<Tuple, R>> resolver, ParameterSpaceSpec parameterSpaceSpec) {
    return createReference(name, parser, resolver, parameterSpaceSpec);
  }

  public static <T, R> ParameterFactory<Parameter<T>, T, R> createReference(String name, Function<String, T> parser, Function<T, Function<Tuple, R>> resolver, ParameterSpaceSpec parameterSpaceSpec) {
    return new ParameterFactoryImpl<>(name,
                                      parser,
                                      resolver,
                                      isSeed(parameterSpaceSpec,
                                  name,
                                  parameterSpaceSpec.parameterNames()),
                                      parameterSpaceSpec);
  }

  private static List<Constraint> createConstraints(boolean isSeed, ParameterSpaceSpec parameterSpaceSpec, String parameterName) {
    return isSeed ? emptyList()
                  : ParameterFactoryImpl.nonSeedAttributeMustBeReferencedAtLeastOnce(parameterSpaceSpec, parameterName)
                                        .map(Collections::singletonList)
                                        .orElse(emptyList());
  }

  private static boolean isSeed(ParameterSpaceSpec parameterSpaceSpec, String attribute, List<String> attributeNames) {
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
}
