package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.*;
import com.github.jcunit.core.model.ParameterFactory;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.factorspace.ParameterSpace;
import org.junit.jupiter.api.extension.*;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils.validateParameterSpaceDefinitionClass;
import static com.github.valid8j.classic.Validates.validate;
import static com.github.valid8j.fluent.Expectations.require;
import static com.github.valid8j.fluent.Expectations.value;
import static com.github.valid8j.pcond.forms.Predicates.isNotNull;
import static java.lang.String.format;
import static java.util.Arrays.asList;
import static java.util.Collections.singletonList;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class JCUnitTestExtension implements BeforeAllCallback, TestTemplateInvocationContextProvider {
  private final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(JCUnitTestExtension.class);
  
  public JCUnitTestExtension() {
  }
  
  
  @Override
  public void beforeAll(ExtensionContext context) {
    // @formatter:off
    List<String> errors = new LinkedList<>();
    validateParameterSpaceDefinitionClass(errors, resolveParameterSpaceClass(context.getTestClass().orElseThrow(AssertionError::new)));
    require(value(errors).satisfies().empty());
    context.getStore(namespace)
           .put("parameterSpace", builParameterSpace(resolveParameterSpaceDefinition(context)));
    // @formatter:on
  }
  
  private static ParameterSpace builParameterSpace(DefineParameterSpace parameterSpaceDefinition) {
    ParameterSpace.Builder builder = new ParameterSpace.Builder();
    List<Constraint> dependencyConstraints = new LinkedList<>();
    Arrays.stream(parameterSpaceDefinition.parameters())
        .map((DefineParameter parameterDefinition) -> createParameter(parameterDefinition, dependencyConstraints))
        .forEach(builder::addParameter);
    Stream.concat(
            dependencyConstraints.stream(),
            Arrays.stream(parameterSpaceDefinition.constraints())
                .map(JCUnitTestExtension::createConstraint))
        .forEach(builder::addConstraint);
    return builder.build();
  }
  
  
  private static Parameter<?> createParameter(DefineParameter parameterDefinition, List<Constraint> dependencyConstraints) {
    return createParameterFactory(parameterDefinition).toParameter(parameterDefinition.name(), asList(parameterDefinition.with()));
  }
  
  private static DefineParameterSpace resolveParameterSpaceDefinition(ExtensionContext context) {
    return context.getTestClass()
        .map(JCUnitTestExtension::resolveParameterSpaceClass)
        .map(JCUnitTestExtension::parameterSpaceDefinitionOf)
        .orElseThrow(AssertionError::new);
  }
  
  
  private static DefineParameterSpace parameterSpaceDefinitionOf(Class<?> c) {
    return validate(c.getAnnotation(DefineParameterSpace.class), isNotNull(), parameterSpaceDefinitionWasNotPresent(c));
  }
  
  private static Function<String, RuntimeException> parameterSpaceDefinitionWasNotPresent(Class<?> c) {
    return detailMessage -> parameterSpaceDefinitionWasNotPresent(c, detailMessage);
  }
  
  private static RuntimeException parameterSpaceDefinitionWasNotPresent(Class<?> c, String msg) {
    return new RuntimeException(format("@%s annotation was not present at %s%n%s",
        DefineParameterSpace.class.getSimpleName(),
        c.getCanonicalName(),
        msg));
  }
  
  private static Class<?> resolveParameterSpaceClass(Class<?> c) {
    Class<?> ret = c.getAnnotation(UsingParameterSpace.class).value();
    return Object.class.equals(ret) ?
        c :
        ret;
  }
  
  private static Constraint createConstraint(DefineConstraint defineConstraint) {
    // TODO
    return Constraint.create("name", x -> true, "");
  }
  
  
  private static ParameterFactory<?, ?> createParameterFactory(DefineParameter parameterDefinition) {
    try {
      return parameterDefinition.as().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw new RuntimeException(e);
    }
  }
  
  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return context.getTestMethod()
        .filter(m -> m.isAnnotationPresent(JCUnitTest.class))
        .isPresent();
  }
  
  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
    return Stream.of(
        new TestTemplateInvocationContext() {
          @Override
          public List<Extension> getAdditionalExtensions() {
            return singletonList(
                new ParameterResolver() {
                  @Override
                  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return true;
                  }
                  
                  @Override
                  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return "HELLO:" + parameterContext.getIndex();
                  }
                }
            );
          }
        },
        new TestTemplateInvocationContext() {
          @Override
          public List<Extension> getAdditionalExtensions() {
            return singletonList(
                new ParameterResolver() {
                  @Override
                  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return true;
                  }
                  
                  @Override
                  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                    return "WORLD";
                  }
                }
            );
          }
        });
  }
}