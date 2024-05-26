package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.JCUnitTest;
import com.github.jcunit.annotations.UsingParameterSpace;
import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.Config;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.testsuite.TestCase;
import org.junit.jupiter.api.extension.*;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils.validateParameterSpaceDefinitionClass;
import static com.github.valid8j.fluent.Expectations.require;
import static com.github.valid8j.fluent.Expectations.value;
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
                                    buildParameterSpace(resolveParameterSpaceDefinition(context.getTestClass().orElseThrow(AssertionError::new)))));
  }

  private static Config configure() {
    return new Config.Builder(requirement()).build();
  }

  private static ParameterSpace buildParameterSpace(ParameterSpaceSpec parameterSpaceSpec) {
    ParameterSpace.Builder builder = new ParameterSpace.Builder();
    /*
    List<Constraint> dependencyConstraints = new LinkedList<>();
    Arrays.stream(parameterSpaceDefinition.parameters())
          .map((DefineParameter parameterDefinition) -> createParameter(parameterDefinition, dependencyConstraints))
          .forEach(builder::addParameter);
    Stream.concat(dependencyConstraints.stream(),
                  Arrays.stream(parameterSpaceDefinition.constraints())
                        .map(JCUnitTestExtension::createConstraint))
          .forEach(builder::addConstraint);

     */
    return builder.build();
  }

  private static ParameterSpaceSpec resolveParameterSpaceDefinition(Class<?> testClass) {
    return null;
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

  private static Class<?> resolveParameterSpaceClass(Class<?> c) {
    Class<?> ret = c.getAnnotation(UsingParameterSpace.class).value();
    return Object.class.equals(ret) ? c : ret;
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