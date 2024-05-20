package com.github.jcunit.core.model;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;
import com.github.valid8j.pcond.forms.Printables;

import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.jcunit.factorspace.Factor.VOID;
import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * An interface that models a specification of a parameter.
 *
 * @param <E> Execution-time parameter type
 * // @formatter:on
 */
public interface ParameterSpec<E> {
  /**
   * A name of the parameter specified by this interface instance.
   *
   * @return The name of parameter.
   */
  String name();

  /**
   * Returns names of parameters on which this parameter depends.
   *
   * @return Names of parameters on which this parameter depends.
   */
  default List<String> dependencies() {
    return valueResolvers().stream()
                           .flatMap(v -> v.dependencies().stream())
                           .distinct()
                           .collect(toList());
  }

  /***
   * Returns {@link ValueResolver}s which represent values this parameter can hold.
   *
   * @return {@code ValueResolver}s.
   */
  List<ValueResolver<E>> valueResolvers();

  default Parameter<ValueResolver<E>> create(ParameterSpaceSpec parameterSpaceSpec) {
    return this.create(parameterSpaceSpec, ParameterSpec::createSimpleParameter);
  }

  default Parameter<ValueResolver<E>> create(ParameterSpaceSpec parameterSpaceSpec,
                                             BiFunction<ParameterSpec<E>, ParameterSpaceSpec, Parameter<ValueResolver<E>>> factory) {
    return factory.apply(this, parameterSpaceSpec);
  }

  static <E> Parameter.Simple.Impl<ValueResolver<E>> createSimpleParameter(ParameterSpec<E> parameterSpec, ParameterSpaceSpec parameterSpaceSpec) {
    boolean isSeed = Utils.isSeed(parameterSpaceSpec, parameterSpec.name(), parameterSpaceSpec.parameterNames());
    return new Parameter.Simple.Impl<>(isSeed,
                                       parameterSpec.name(),
                                       parameterSpec.valueResolvers(),
                                       Utils.createConstraints(isSeed,
                                                               parameterSpaceSpec,
                                                               parameterSpec.name()));
  }

  enum Utils {
    ;

    private static boolean isSeed(ParameterSpaceSpec parameterSpaceSpec, String attribute, List<String> attributeNames) {
      Set<String> referencingAttributes = new HashSet<>();
      for (String each : attributeNames) {
        if (Objects.equals(each, attribute)) continue;
        if (areAllPossibleValuesReferencing(parameterSpaceSpec, attribute, each)) return true;
        if (anyPossibleValueReferencing(parameterSpaceSpec, attribute, each)) referencingAttributes.add(each);
      }
      return referencingAttributes.isEmpty();
    }

    private static boolean areAllPossibleValuesReferencing(ParameterSpaceSpec parameterSpaceSpec,
                                                           String referencingAttribute,
                                                           String referencedAttribute) {
      return parameterSpaceSpec.possibleValidValuesFor(referencingAttribute)
                               .stream()
                               .allMatch(eachPossibleValue -> parameterSpaceSpec.referencesFor(referencingAttribute)
                                                                                .apply(eachPossibleValue)
                                                                                .contains(referencedAttribute));
    }

    private static boolean anyPossibleValueReferencing(ParameterSpaceSpec parameterSpaceSpec,
                                                       String referencingAttribute,
                                                       String referencedAttribute) {
      return parameterSpaceSpec.possibleValidValuesFor(referencingAttribute)
                               .stream()
                               .anyMatch(eachPossibleValue -> parameterSpaceSpec.referencesFor(referencingAttribute)
                                                                                .apply(eachPossibleValue)
                                                                                .contains(referencedAttribute));
    }

    private static List<Constraint> createConstraints(boolean isSeed,
                                                      ParameterSpaceSpec parameterSpaceSpec,
                                                      String parameterName) {
      return isSeed ? emptyList()
                    : nonSeedAttributeMustBeReferencedAtLeastOnce(parameterSpaceSpec,
                                                                  parameterName).map(Collections::singletonList)
                                                                                .orElse(emptyList());
    }

    private static Optional<Constraint> nonSeedAttributeMustBeReferencedAtLeastOnce(ParameterSpaceSpec parameterSpaceSpec,
                                                                                    String referencedAttribute) {
      if (parameterSpaceSpec.parameterNames().isEmpty()) {
        return Optional.empty();
      }
      String[] conditionallyReferencingAttributes = parameterSpaceSpec.parameterNames()
                                                                      .stream()
                                                                      .filter(each -> !Objects.equals(referencedAttribute, each))
                                                                      .filter(each -> anyPossibleValueReferencing(parameterSpaceSpec, each, referencedAttribute))
                                                                      .toArray(String[]::new);
      return Optional.of(Constraint.create("nonSeedAttributeMustBeReferencedAtLeastOnce:" + referencedAttribute,
                                           tuple -> tuple.get(referencedAttribute).equals(VOID) != referencedByAnyAttribute(tuple,
                                                                                                                            referencedAttribute,
                                                                                                                            conditionallyReferencingAttributes,
                                                                                                                            parameterSpaceSpec),
                                           Stream.concat(Stream.of(referencedAttribute),
                                                         Arrays.stream(conditionallyReferencingAttributes))
                                                 .toArray(String[]::new)));
    }

    private static boolean referencedByAnyAttribute(Tuple tuple,
                                                    String referencedAttribute,
                                                    String[] conditionallyReferencingAttributes,
                                                    ParameterSpaceSpec parameterSpaceSpec) {
      for (String eachReferencingAttribute : conditionallyReferencingAttributes) {
        if (parameterSpaceSpec.referencesFor(eachReferencingAttribute)
                              .apply(tuple.get(eachReferencingAttribute))
                              .contains(referencedAttribute))
          return true;
      }
      return false;
    }
  }

  interface ValueResolver<V> {
    V resolve(Tuple testData);

    List<String> dependencies();

    static <V> ValueResolver<V> simple(V value) {
      return create(Printables.function("value[" + value + "]", x -> value), emptyList());
    }

    static <V> ValueResolver<V> create(Function<Tuple, V> resolver,
                                       List<String> dependencies) {
      return new ValueResolver<V>() {
        @Override
        public V resolve(Tuple testData) {
          return resolver.apply(testData);
        }

        @Override
        public List<String> dependencies() {
          return dependencies;
        }
      };
    }
  }
}

