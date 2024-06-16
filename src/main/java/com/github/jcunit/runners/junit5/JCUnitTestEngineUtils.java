package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.Named;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.model.ParameterSpaceSpec;
import com.github.jcunit.model.ParameterSpec;
import com.github.jcunit.model.ValueResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * // @formatter:off
 * // @formatter:on
 */
public enum JCUnitTestEngineUtils {
  ;

  static void validateNoNameDuplications(List<String> errors, Map<String, List<Object>> nameDefinitionsMap) {
    nameDefinitionsMap.keySet()
                      .stream()
                      .filter(k -> nameDefinitionsMap.get(k).size() != 1)
                      .peek(k -> {
                        assert !k.isEmpty();
                      })
                      .forEach(k -> errors.add(format("Name:'%s' has duplicated definitions: [%s]", k, nameDefinitionsMap.get(k))));
  }

  static void validateReferencesOfConstraints(List<String> errors, ParameterSpaceSpec parameterSpaceSpec, Set<String> knownNames) {
    parameterSpaceSpec.constraints()
                      .forEach(c -> validateConstraintDefinition(errors, c, knownNames));
  }

  static void validateReferencesOfParameters(List<String> errors, ParameterSpaceSpec parameterSpaceSpec, Set<String> knownNames) {
    parameterSpaceSpec.parameterNames()
                      .stream()
                      .map(parameterSpaceSpec::parameterSpecFor)
                      .forEach(p -> validateParameterDefinition(errors, p, parameterSpaceSpec, knownNames));
  }

  private static void validateParameterDefinition(List<String> errors, ParameterSpec<?> parameterSpec, ParameterSpaceSpec parameterSpaceDefinitionClass, Set<String> knownNames) {
    dependenciesOfParameterSpec(parameterSpec)
        .stream()
        .filter(each -> !knownNames.contains(each))
        .forEach(each -> errors.add(format("Parameter:'%s' depends on unknown name:'%s'", parameterSpec.name(), each)));
  }

  private static void validateConstraintDefinition(List<String> errors, Constraint constraintDefinition, Set<String> knownNames) {
    dependenciesOfConstraintDefinition(constraintDefinition)
        .stream()
        .filter(each -> !knownNames.contains(each))
        .forEach(each -> errors.add(String.format("Constraint:'%s' depends on unknown name:'%s'", constraintDefinition.getName(), each)));
  }

  static void validateNamedMethod(List<String> errors, Method method) {
    if (!Modifier.isStatic(method.getModifiers()))
      errors.add("Method is not static: " + method);
    if (!Modifier.isPublic(method.getModifiers()))
      errors.add("Method is not public: " + method);
    for (int i = 0; i < method.getParameterCount(); i++) {
      Optional<From> from = Optional.empty();
      for (Annotation eachAnnotation : method.getParameterAnnotations()[i]) {
        if (eachAnnotation instanceof From) {
          from = Optional.of((From) eachAnnotation);
        }
      }
      if (!from.isPresent())
        errors.add(format("@From annotation is not present for parameters[%s] of method:'%s'", i, method));
    }
  }

  static Set<String> dependenciesOfParameterSpec(ParameterSpec<?> parameterSpec) {
    return parameterSpec.valueResolvers()
                        .stream()
                        .map(ValueResolver::dependencies)
                        .flatMap(Collection::stream)
                        .collect(toSet());
  }

  static Set<String> dependenciesOfConstraintDefinition(Constraint constraint) {
    return new HashSet<>(constraint.involvedKeys());
  }

  public static String nameOf(Method method) {
    assert method.isAnnotationPresent(Named.class);
    if ("".equals(method.getAnnotation(Named.class).value()))
      return method.getName();
    return method.getAnnotation(Named.class).value();
  }

  static Map<String, List<Object>> definedNamesInParameterSpace(Class<?> testModelClass) {
    Map<String, List<Object>> ret = new HashMap<>();
    Arrays.stream(testModelClass.getMethods())
        .filter(m -> m.isAnnotationPresent(Named.class))
          .forEach(m -> {
            String name = nameOf(m);
            ret.putIfAbsent(name, new ArrayList<>());
            ret.get(name).add(m);
          });
    return ret;
  }

  static List<Method> namedMethodsFromParameterSpaceClass(Class<?> parameterSpaceDefinitionClass) {
    return Arrays.stream(parameterSpaceDefinitionClass.getDeclaredMethods())
                 .filter(eachMethod -> eachMethod.isAnnotationPresent(Named.class))
                 .collect(toList());
  }
}
