package com.github.jcunit.core.model;

import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.Config;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.testsuite.TestSuite;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface ParameterSpaceSpec {
  static ParameterSpaceSpec create(List<ParameterSpec<?>> parameterSpecs, List<Constraint> constraintList) {
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

      @Override
      public List<Constraint> constraints() {
        return constraintList;
      }

      @SuppressWarnings("unchecked")
      @Override
      public <E> ParameterSpec<E> parameterSpecFor(String parameterName) {
        return (ParameterSpec<E>) parameterSpecMap.get(parameterName);
      }
    };
  }

  List<String> parameterNames();

  Function<Object, Set<String>> referencesFor(String parameterName);

  <T> List<T> possibleValidValuesFor(String parameterName);

  List<Constraint> constraints();

  <E> ParameterSpec<E> parameterSpecFor(String parameterName);

  default ParameterSpace toParameterSpace() {
    ParameterSpace.Builder b = new ParameterSpace.Builder();
    this.parameterNames()
        .stream()
        .map(this::parameterSpecFor)
        .map(each -> each.toParameter(this))
        .forEach(b::addParameter);
    this.constraints()
        .forEach(b::addConstraint);
    return b.build();
  }

  default TestSuite toTestSuite() {
    return Pipeline.Standard.create().execute(config(), toParameterSpace());

  }

  default Config config() {
    return new Config.Builder(requirement()).build();
  }

  default Requirement requirement() {
    return new Requirement.Builder().build();
  }

}
