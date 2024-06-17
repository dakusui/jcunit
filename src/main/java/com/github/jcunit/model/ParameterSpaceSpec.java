package com.github.jcunit.model;

import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.ParameterSpace;

import java.util.*;
import java.util.function.Function;

import static java.util.stream.Collectors.toMap;
import static java.util.stream.Collectors.toSet;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public interface ParameterSpaceSpec {
  static ParameterSpaceSpec create(List<ParameterSpec<?>> parameterSpecs,
                                   List<Constraint> constraintList) {
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

  default ParameterSpace toParameterSpace(List<String> seedParameterNames) {
    Set<String> optionals = Utils.optionalParameterNamesIn(this.parameterNames().stream().map(this::parameterSpecFor).collect(toSet()), seedParameterNames);
    ParameterSpace.Builder b = new ParameterSpace.Builder();
    this.parameterNames()
        .stream()
        .map(this::parameterSpecFor)
        .map(each -> each.toParameter(this, optionals.contains(each.name())))
        .forEach(b::addParameter);
    this.constraints()
        .forEach(b::addConstraint);
    return b.build();
  }

  enum Utils {
    ;

    private static Set<String> optionalParameterNamesIn(Collection<ParameterSpec<?>> allParameterSpecs, Collection<String> seedParameterNames) {
      return optionalParameterNamesIn(allParameterSpecs.stream()
                                                       .collect(toMap(ParameterSpec::name, e -> e)), seedParameterNames
      );
    }

    private static Set<String> optionalParameterNamesIn(Map<String, ParameterSpec<?>> allParameterSpecs, Collection<String> seedParameterNames) {
      Set<String> ret = reachableParameterNamesIn(allParameterSpecs, seedParameterNames);
      ret.removeAll(requiredParameterNamesIn(allParameterSpecs, seedParameterNames));
      return ret;
    }

    private static Set<String> reachableParameterNamesIn(Map<String, ParameterSpec<?>> allParameterSpecs, Collection<String> seedParameterNames) {
      return seedParameterNames.stream()
                               .flatMap(n -> reachableParameterNamesFrom(allParameterSpecs.get(n),
                                                                         new HashSet<>(allParameterSpecs.values())).stream())
                               .collect(toSet());
    }

    private static Set<String> requiredParameterNamesIn(Map<String, ParameterSpec<?>> allParameterSpecs, Collection<String> seedParameterNames) {
      return seedParameterNames.stream()
                               .flatMap(n -> requiredParameterNamesBy(allParameterSpecs.get(n),
                                                                      new HashSet<>(allParameterSpecs.values())).stream())
                               .collect(toSet());
    }

    private static Set<String> reachableParameterNamesFrom(ParameterSpec<?> cur, Set<ParameterSpec<?>> allParameterSpecs) {
      Set<String> ret = new HashSet<>();
      Set<ParameterSpec<?>> visited = new HashSet<>();
      reachableParameterNamesFrom(ret, cur, visited, allParameterSpecs);
      return ret;
    }

    private static void reachableParameterNamesFrom(Set<String> out, ParameterSpec<?> cur, Set<ParameterSpec<?>> visited, Set<ParameterSpec<?>> allParameterSpecs) {
      if (visited.contains(cur)) {
        return;
      }
      visited.add(cur);
      out.addAll(directlyReachableParameterNamesFrom(cur));
      allParameterSpecs.forEach(p -> reachableParameterNamesFrom(out, p, visited, allParameterSpecs));
    }

    private static Set<String> directlyReachableParameterNamesFrom(ParameterSpec<?> parameterSpec) {
      return parameterSpec.valueResolvers()
                          .stream()
                          .flatMap(r -> r.dependencies().stream())
                          .collect(toSet());
    }

    private static Set<String> requiredParameterNamesBy(ParameterSpec<?> cur, Set<ParameterSpec<?>> allParameterSpecs) {
      Set<String> ret = new HashSet<>();
      Set<ParameterSpec<?>> visited = new HashSet<>();
      requiredParameterNamesBy(ret, cur, visited, allParameterSpecs);
      return ret;
    }


    private static void requiredParameterNamesBy(Set<String> out, ParameterSpec<?> cur, Set<ParameterSpec<?>> visited, Set<ParameterSpec<?>> allParameterSpecs) {
      if (visited.contains(cur)) {
        return;
      }
      visited.add(cur);
      out.addAll(directlyRequiredParameterNamesBy(cur));
      allParameterSpecs.forEach(p -> reachableParameterNamesFrom(out, p, visited, allParameterSpecs));
    }

    static Set<String> directlyRequiredParameterNamesBy(ParameterSpec<?> parameterSpec) {
      Set<String> requiredParameterNames = null;
      for (ValueResolver<?> r : parameterSpec.valueResolvers()) {
        if (requiredParameterNames == null) requiredParameterNames = new HashSet<>(r.dependencies());
        else requiredParameterNames.retainAll(r.dependencies());
        if (requiredParameterNames.isEmpty()) {
          break;
        }
      }
      return requiredParameterNames == null ? new HashSet<>()
                                            : requiredParameterNames;
    }
  }
}
