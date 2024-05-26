package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.Named;
import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.model.ParameterSpec;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.factorspace.Constraint;

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
public enum JCUnitTestExtensionUtils {
  ;

  static void validateParameterSpaceDefinitionClass(List<String> errors, Class<?> parameterSpaceSpecClass) {
    ParameterSpaceSpec parameterSpaceSpec = parameterSpaceDefinitionFromClass(parameterSpaceSpecClass).orElseThrow(RuntimeException::new);
    Map<String, List<Object>> knownNamesInParameterSpace = definedNamesInParameterSpace(parameterSpaceSpec);
    validateNameDefinitionsInParameterSpace(errors, knownNamesInParameterSpace);
    validateParameterSpaceDefinition(errors, parameterSpaceSpec, parameterSpaceSpecClass, knownNamesInParameterSpace.keySet());
    namedMethodsFromParameterSpaceClass(parameterSpaceSpecClass).forEach(
        eachNamedMethod -> validateNamedMethod(errors, eachNamedMethod)
    );
  }

  private static void validateNameDefinitionsInParameterSpace(List<String> errors, Map<String, List<Object>> nameDefinitionsMap) {
    nameDefinitionsMap.keySet()
                      .stream()
                      .filter(k -> nameDefinitionsMap.get(k).size() != 1)
                      .peek(k -> {
                        assert !k.isEmpty();
                      })
                      .forEach(k -> errors.add(format("Name:'%s' has duplicated definitions: [%s]", k, nameDefinitionsMap.get(k))));
  }

  private static void validateParameterSpaceDefinition(List<String> errors, ParameterSpaceSpec parameterSpaceSpec, Class<?> parameterSpaceDefinitionClass, Set<String> knownNames) {
    parameterSpaceSpec.parameterNames()
                      .stream()
                      .map(parameterSpaceSpec::parameterSpecFor)
                      .forEach(p -> validateParameterDefinition(errors, p, parameterSpaceSpec, knownNames));
    parameterSpaceSpec.constraints()
                      .forEach(c -> validateConstraintDefinition(errors, c, parameterSpaceDefinitionClass, knownNames));
  }

  private static void validateParameterDefinition(List<String> errors, ParameterSpec<?> parameterSpec, ParameterSpaceSpec parameterSpaceDefinitionClass, Set<String> knownNames) {
    dependenciesOfParameterSpec(parameterSpec)
        .stream()
        .filter(each -> !knownNames.contains(each))
        .forEach(each -> errors.add(format("Parameter:'%s' depends on unknown name:'%s'", parameterSpec.name(), each)));
  }

  private static void validateConstraintDefinition(List<String> errors, Constraint constraintDefinition, Class<?> parameterSpaceDefinitionClass, Set<String> knownNames) {
    dependenciesOfConstraintDefinition( constraintDefinition)
        .stream()
        .filter(each -> !knownNames.contains(each))
        .forEach(each -> errors.add(String.format("Constraint:'%s' depends on unknown name:'%s'", constraintDefinition.getName(), each)));
  }

  private static void validateNamedMethod(List<String> errors, Method method) {
    if (!Modifier.isStatic(method.getModifiers()))
      errors.add("");
    if (!Modifier.isPublic(method.getModifiers()))
      errors.add("");
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

  static Optional<ParameterSpaceSpec> parameterSpaceDefinitionFromClass(Class<?> parameterSpaceDefinitionClass) {
    //  TODO //
    return Optional.empty();
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

  static String nameOf(Method method) {
    assert method.isAnnotationPresent(Named.class);
    if ("".equals(method.getAnnotation(Named.class).value()))
      return method.getName();
    return method.getAnnotation(Named.class).value();
  }

  static Map<String, List<Object>> definedNamesInParameterSpace(ParameterSpaceSpec parameterSpaceSpec) {
    Map<String, List<Object>> ret = new HashMap<>();
    parameterSpaceSpec.parameterNames().forEach(parameterName -> {
      ret.computeIfAbsent(parameterName, k -> new LinkedList<>());
      ret.get(parameterName).add(parameterName);
    });
    parameterSpaceSpec.parameterNames().forEach(n -> {
      ret.computeIfAbsent(n, k -> new LinkedList<>());
      ret.get(n).add(parameterSpaceSpec.parameterSpecFor(n));
    });
    parameterSpaceSpec.constraints().forEach(c -> {
      if (!"".equals(c.getName())) {
        ret.computeIfAbsent(c.getName(), k -> new LinkedList<>());
        ret.get(c.getName()).add(c);
      }
    });
    return ret;
  }

  static List<Method> namedMethodsFromParameterSpaceClass(Class<?> parameterSpaceDefinitionClass) {
    return Arrays.stream(parameterSpaceDefinitionClass.getDeclaredMethods())
                 .filter(eachMethod -> eachMethod.isAnnotationPresent(Named.class))
                 .collect(toList());
  }
}
