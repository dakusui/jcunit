package com.github.jcunit.core.model;

import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface ParameterSpaceSpec {
  List<String> parameterNames();

  Function<Object, Set<String>> referencesFor(String parameterName);

  <T> List<T> possibleValidValuesFor(String parameterName);
}
