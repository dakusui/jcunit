package com.github.jcunit.core.model;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface ParameterSpaceSpec {
  static ParameterSpaceSpec createParameterSpaceSpec(List<String> parameterNames, Function<String, Function<Object, Set<String>>> references, Function<String, List<Object>> possibleValues) {
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

  List<String> parameterNames();

  Function<Object, Set<String>> referencesFor(String parameterName);

  <T> List<T> possibleValidValuesFor(String parameterName);
}
