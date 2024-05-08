package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.*;
import com.github.jcunit.core.model.ParameterResolver;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.emptySet;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * // @formatter:on
 */
public enum JCUnitTestExtensionUtils {
  ;
  
  static void validateParameterSpaceDefinitionClass(List<String> errors, Class<?> parameterSpaceDefinitionClass) {
    DefineParameterSpace parameterSpaceDefinition = parameterSpaceDefinitionFromClass(parameterSpaceDefinitionClass).orElseThrow(RuntimeException::new);
    Map<String, List<Object>> knownNamesInParameterSpace = definedNamesInParameterSpace(parameterSpaceDefinition, parameterSpaceDefinitionClass);
    validateNameDefinitionsInParameterSpace(errors, knownNamesInParameterSpace);
    validateParameterSpaceDefinition(errors, parameterSpaceDefinition, parameterSpaceDefinitionClass, knownNamesInParameterSpace.keySet());
    namedMethodsFromParameterSpaceClass(parameterSpaceDefinitionClass).forEach(
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
  
  private static void validateParameterSpaceDefinition(List<String> errors, DefineParameterSpace parameterSpaceDefinition, Class<?> parameterSpaceDefinitionClass, Set<String> knownNames) {
    Arrays.stream(parameterSpaceDefinition.parameters()).forEach(p -> validateParameterDefinition(errors, p, parameterSpaceDefinitionClass, knownNames));
    Arrays.stream(parameterSpaceDefinition.constraints()).forEach(c -> validateConstraintDefinition(errors, c, parameterSpaceDefinitionClass, knownNames));
  }
  
  private static void validateParameterDefinition(List<String> errors, DefineParameter parameterDefinition, Class<?> parameterSpaceDefinitionClass, Set<String> knownNames) {
    dependenciesOfParameterDefinition(parameterSpaceDefinitionClass, parameterDefinition)
        .stream()
        .filter(each -> !knownNames.contains(each))
        .forEach(each -> errors.add(format("Parameter:'%s' depends on unknown name:'%s'", parameterDefinition.name(), each)));
  }
  
  private static void validateConstraintDefinition(List<String> errors, DefineConstraint constraintDefinition, Class<?> parameterSpaceDefinitionClass, Set<String> knownNames) {
    dependenciesOfConstraintDefinition(parameterSpaceDefinitionClass, constraintDefinition)
        .stream()
        .filter(each -> !knownNames.contains(each))
        .forEach(each -> errors.add(String.format("Constraint:'%s' depends on unknown name:'%s'", constraintNameOrSummary(constraintDefinition), each)));
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
  
  static String constraintNameOrSummary(DefineConstraint constraintDefinition) {
    return !"".equals(constraintDefinition.name()) ?
        constraintDefinition.name() :
        Arrays.stream(constraintDefinition.value()).collect(Collectors.joining(")&&(", "(", ")"));
  }
  
  static Optional<DefineParameterSpace> parameterSpaceDefinitionFromClass(Class<?> parameterSpaceDefinitionClass) {
    return parameterSpaceDefinitionClass.isAnnotationPresent(DefineParameterSpace.class) ?
        Optional.of(parameterSpaceDefinitionClass.getAnnotation(DefineParameterSpace.class)) :
        Optional.empty();
  }
  
  static Set<String> dependenciesOfParameterDefinition(Class<?> parameterSpaceDefinitionClass, DefineParameter parameterDefinition) {
    if (parameterDefinition.as().equals(ParameterResolver.class)) {
      return Arrays.stream(parameterSpaceDefinitionClass.getMethods())
          .filter(m -> nameOf(m).equals(parameterDefinition.name()))
          .map(JCUnitTestExtensionUtils::dependenciesOf)
          .findFirst()
          .orElse(emptySet());
    }
    return emptySet();
  }
  
  static Set<String> dependenciesOfConstraintDefinition(Class<?> parameterSpaceDefinitionClass, DefineConstraint parameterSpaceDefinition) {
    return Arrays.stream(parameterSpaceDefinition.value())
        .flatMap(c -> Arrays.stream(c.split("&&")).map(t -> t.replace("!", "")))
        .map(n -> nameToMethod(parameterSpaceDefinitionClass, n))
        .flatMap(m -> m.map(JCUnitTestExtensionUtils::dependenciesOf).orElse(Collections.emptySet()).stream())
        .collect(Collectors.toSet());
  }
  
  static Set<String> dependenciesOf(Method method) {
    return Arrays.stream(method.getParameters())
        .map(each -> each.getAnnotation(From.class).value())
        .collect(Collectors.toSet());
  }
  
  static String nameOf(Method method) {
    assert method.isAnnotationPresent(Named.class);
    if ("".equals(method.getAnnotation(Named.class).value()))
      return method.getName();
    return method.getAnnotation(Named.class).value();
  }
  
  static Map<String, List<Object>> definedNamesInParameterSpace(DefineParameterSpace parameterSpaceDefinition, Class<?> parameterSpaceClass) {
    Map<String, List<Object>> ret = new HashMap<>();
    namedMethodsFromParameterSpaceClass(parameterSpaceClass).forEach(m -> {
      String nameOfMethod = nameOf(m);
      ret.computeIfAbsent(nameOfMethod, k -> new LinkedList<>());
      ret.get(nameOfMethod).add(m);
    });
    Arrays.stream(parameterSpaceDefinition.parameters()).forEach(p -> {
      ret.computeIfAbsent(p.name(), k -> new LinkedList<>());
      ret.get(p.name()).add(p);
    });
    Arrays.stream(parameterSpaceDefinition.constraints()).forEach(c -> {
      if (!"".equals(c.name())) {
        ret.computeIfAbsent(c.name(), k -> new LinkedList<>());
        ret.get(c.name()).add(c);
      }
    });
    return ret;
  }
  
  static Optional<Method> nameToMethod(Class<?> parameterSpaceDefinitionClass, String elementName) {
    return Arrays.stream(parameterSpaceDefinitionClass.getMethods())
        .filter(m -> nameOf(m).equals(elementName))
        .findFirst();
  }
  
  static List<Method> namedMethodsFromParameterSpaceClass(Class<?> parameterSpaceDefinitionClass) {
    return Arrays.stream(parameterSpaceDefinitionClass.getDeclaredMethods())
        .filter(eachMethod -> eachMethod.isAnnotationPresent(Named.class))
        .collect(toList());
  }
}
