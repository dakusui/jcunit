package com.github.jcunit.core.model;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface ParameterSpaceSpec {
  static ParameterSpaceSpec createParameterSpaceSpec(List<ParameterSpec<?>> parameterSpecs) {
    Map<String, ParameterSpec<?>> parameterSpecMap = parameterSpecs.stream()
                                                                   .collect(toMap(ParameterSpec::name,
                                                                                  Function.identity()));
    return new ParameterSpaceSpec() {
      @Override
      public List<String> parameterNames() {
        return new ArrayList<>(parameterSpecMap.keySet());
      }

      @Override
      public Function<Object, Set<String>> referencesFor(String parameterName) {
        return o -> new HashSet<String>(parameterSpecMap.get(parameterName)
                                                        .dependencies());
      }

      @SuppressWarnings("unchecked")
      @Override
      public <TT> List<TT> possibleValidValuesFor(String parameterName) {
        return (List<TT>) parameterSpecMap.get(parameterName).valueResolvers();
      }
    };
  }

  List<String> parameterNames();

  Function<Object, Set<String>> referencesFor(String parameterName);

  <T> List<T> possibleValidValuesFor(String parameterName);

}
