package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.*;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.Parameter;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.Config;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.testsuite.TestCase;
import org.junit.jupiter.api.extension.*;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils.validateParameterSpaceDefinitionClass;
import static com.github.valid8j.classic.Validates.validate;
import static com.github.valid8j.fluent.Expectations.require;
import static com.github.valid8j.fluent.Expectations.value;
import static com.github.valid8j.pcond.forms.Predicates.isNotNull;
import static java.lang.String.format;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

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
    List<String> errors = new LinkedList<>();
    validateParameterSpaceDefinitionClass(errors, resolveParameterSpaceClass(context.getTestClass().orElseThrow(AssertionError::new)));
    require(value(errors).satisfies().empty());
    context.getStore(namespace)
           .put("testDataSet",
                generateTestDataSet(configure(),
                                    buildParameterSpace(resolveParameterSpaceDefinition(context))));
  }

  private static Config configure() {
    return new Config.Builder(requirement()).build();
  }

  private static ParameterSpace buildParameterSpace(DefineParameterSpace parameterSpaceDefinition) {
    ParameterSpace.Builder builder = new ParameterSpace.Builder();
    List<Constraint> dependencyConstraints = new LinkedList<>();
    Arrays.stream(parameterSpaceDefinition.parameters())
          .map((DefineParameter parameterDefinition) -> createParameter(parameterDefinition, dependencyConstraints))
          .forEach(builder::addParameter);
    Stream.concat(dependencyConstraints.stream(),
                  Arrays.stream(parameterSpaceDefinition.constraints())
                        .map(JCUnitTestExtension::createConstraint))
          .forEach(builder::addConstraint);
    return builder.build();
  }

  private static List<Tuple> generateTestDataSet(Config config, ParameterSpace parameterSpace) {
    return new Pipeline.Standard().generateTestSuite(config, parameterSpace, null)
                                  .stream()
                                  .map(TestCase::getTestData)
                                  .collect(toList());
  }

  private static Requirement requirement() {
    return new Requirement.Builder().build();
  }


  private static Parameter<?> createParameter(DefineParameter parameterDefinition, List<Constraint> dependencyConstraints) {
    throw new RuntimeException("Not implemented yet");
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
    return new RuntimeException(format("@%s annotation was not present at %s%n%s", DefineParameterSpace.class.getSimpleName(), c.getCanonicalName(), msg));
  }

  private static Class<?> resolveParameterSpaceClass(Class<?> c) {
    Class<?> ret = c.getAnnotation(UsingParameterSpace.class).value();
    return Object.class.equals(ret) ? c : ret;
  }

  private static Constraint createConstraint(DefineConstraint defineConstraint) {
    // TODO
    return Constraint.create("name", x -> true, "");
  }


  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return context.getTestMethod().filter(m -> m.isAnnotationPresent(JCUnitTest.class)).isPresent();
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
    @SuppressWarnings("unchecked") List<Tuple> testDataSet = context.getStore(namespace).get("testDataset", List.class);
    @SuppressWarnings("unchecked") Map<String, Function<Object, Object>> executionTimeResolvers = context.getStore(namespace).get("executionTimeResolvers", Map.class);
    return testDataSet.stream()
                      .map(eachTestDataTuple -> new TestTemplateInvocationContext() {
                        @Override
                        public List<Extension> getAdditionalExtensions() {
                          return singletonList(new ParameterResolver() {
                            @Override
                            public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                              return true;
                            }

                            @Override
                            public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
                              String sourceParameterName = parameterContext.getParameter().getAnnotation(From.class).value();
                              return executionTimeResolvers.get(sourceParameterName).apply(eachTestDataTuple.get(sourceParameterName));
                            }
                          });
                        }
                      });
  }
}