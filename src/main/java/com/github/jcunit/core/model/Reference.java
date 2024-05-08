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
   * @param name           A name of the parameter which is defined by this object.
   * @param parser         A function that parses strings given to {@link DefineParameter#with()} value into generation time parameter value.
   * @param resolver       A function that converts a generation time parameter value into a corresponding execution time parameter value.
   * @param parameterNames All the parameter names in the parameter space this object belongs to.
   * @param references     A function that resolves references between parameters.
   * @param possibleValues A function that resolves all possible valid values of each parameter.
   */
  public Reference(String name,
                   List<String> parameterNames,
                   Function<String, T> parser,
                   Function<T, Function<Tuple, R>> resolver,
                   Function<String, Function<Object, Set<String>>> references,
                   Function<String, List<Object>> possibleValues) {
    this.resolver = resolver;
    this.name = name;
    this.parser = parser;
    this.parameterSpaceSpec = new ParameterSpaceSpec() {
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
    this.isSeed = isSeed(this.name, parameterNames.toArray(new String[0]));
  }

  @Override
  public GenerationTimeParameterFactory<Parameter<T>, T> parameterFactory() {
    return new GenerationTimeParameterFactory<Parameter<T>, T>() {
      @Override
      public Parameter<T> createParameter(String... args) {
        return new Parameter.Simple.Impl<>(!isSeed,
                                           name(),
                                           Arrays.stream(args)
                                                 .map(parser)
                                                 .collect(toList()));
      }

      /**
       * @param possibleReferences Values specified by {@link DefineParameter#with()}.
       *                           Possible values for the parameter specified by {@code name} should be given.
       * @return Created constraints
       */
      @Override
      public List<Constraint> createConstraints(String... possibleReferences) {
        return nonSeedAttributeMustBeReferencedAtLeastOnce(name,
                                                           parameterSpaceSpec.parameterNames()
                                                                             .toArray(new String[0]))
            .map(Collections::singletonList)
            .orElse(emptyList());
      }
    };
  }

  private boolean isSeed(String attribute, String[] attributeNames) {
    Set<String> referencingAttributes = new HashSet<>();
    for (String each : attributeNames) {
      if (Objects.equals(each, attribute))
        continue;
      if (areAllPossibleValuesReferencing(attribute, each))
        return true;
      if (anyPossibleValueReferencing(attribute, each))
        referencingAttributes.add(each);
    }
    return referencingAttributes.isEmpty();
  }

  boolean areAllPossibleValuesReferencing(String referencingAttribute, String referencedAttribute) {
    return parameterSpaceSpec.possibleValidValuesFor(referencingAttribute)
                             .stream()
                             .allMatch(eachPossibleValue -> parameterSpaceSpec.referencesFor(referencingAttribute)
                                                                              .apply(eachPossibleValue)
                                                                              .contains(referencedAttribute));
  }

  boolean anyPossibleValueReferencing(String referencingAttribute, String referencedAttribute) {
    return parameterSpaceSpec.possibleValidValuesFor(referencingAttribute)
                             .stream()
                             .anyMatch(eachPossibleValue -> parameterSpaceSpec.referencesFor(referencingAttribute)
                                                                              .apply(eachPossibleValue)
                                                                              .contains(referencedAttribute));
  }

  Optional<Constraint> nonSeedAttributeMustBeReferencedAtLeastOnce(String referencedAttribute, String... attributes) {
    if (isSeed)
      return Optional.empty();
    if (attributes.length == 0) {
      return Optional.empty();
    }
    String[] referencingAttributes = Arrays.stream(attributes)
                                           .filter(each -> !Objects.equals(referencedAttribute, each))
                                           .filter(each -> anyPossibleValueReferencing(each, referencedAttribute))
                                           .toArray(String[]::new);
    return Optional.of(
        Constraint.create(name,
                          tuple -> tuple.get(referencedAttribute).equals(VOID) != referencedByAnyAttribute(tuple,
                                                                                                           referencedAttribute,
                                                                                                           referencingAttributes),
                          Stream.concat(
                                    Stream.of(referencedAttribute),
                                    Arrays.stream(referencingAttributes))
                                .toArray(String[]::new)));
  }


  private boolean referencedByAnyAttribute(Tuple tuple, String referencedAttribute, String[] conditionallyReferencingAttributes) {
    for (String eachReferencingAttribute : conditionallyReferencingAttributes) {
      if (this.parameterSpaceSpec.referencesFor(eachReferencingAttribute)
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
