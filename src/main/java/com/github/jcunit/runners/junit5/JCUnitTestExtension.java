package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.JCUnitParameter;
import com.github.jcunit.annotations.JCUnitTest;
import com.github.jcunit.annotations.UsingParameterSpace;
import com.github.jcunit.core.model.ParameterSpaceSpec;
import com.github.jcunit.core.model.ParameterSpec;
import com.github.jcunit.core.model.ValueResolver;
import com.github.jcunit.core.model.ValueResolvers;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.pipeline.Config;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.Requirement;
import com.github.jcunit.testsuite.TestCase;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

import static com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils.validateParameterSpaceDefinitionClass;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@TestInstance(PER_CLASS)
public class JCUnitTestExtension implements BeforeAllCallback,
                                            TestTemplateInvocationContextProvider {
  private final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(JCUnitTestExtension.class);

  public JCUnitTestExtension() {
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    {
      List<String> errors = new LinkedList<>();
      validateParameterSpaceDefinitionClass(errors, Utils.resolveParameterSpaceClass(context.getTestClass().orElseThrow(AssertionError::new)));
      //require(value(errors).satisfies().empty());
      ParameterSpaceSpec parameterSpaceSpec = Utils.createParameterSpaceSpec(context.getTestClass().orElseThrow(AssertionError::new));
      List<Tuple> value = Utils.generateTestDataSet(Utils.configure(),
                                                    parameterSpaceSpec.toParameterSpace());
      value.forEach(System.err::println);
      context.getStore(namespace).put("testDataSet", value);
      context.getStore(namespace).put("parameterSpaceSpec", parameterSpaceSpec);
    }
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return context.getTestMethod().filter(m -> m.isAnnotationPresent(JCUnitTest.class)).isPresent();
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
    @SuppressWarnings("unchecked") List<Tuple> testDataSet = context.getStore(namespace).get("testDataSet", List.class);
    testDataSet.forEach(System.err::println);
    return testDataSet.stream().map(Utils::toTestTemplateInvocationContext);
  }

  enum Utils {
    ;

    private static Config configure() {
      return new Config.Builder(requirement()).build();
    }

    static ParameterSpaceSpec createParameterSpaceSpec(Class<?> testModelClass) {
      return ParameterSpaceSpec.create(createParameterSpecsFromModelClass(testModelClass),
                                       createConstraintsFromModelClass(testModelClass));
    }

    private static List<Constraint> createConstraintsFromModelClass(@SuppressWarnings("unused") Class<?> testModelClass) {
      return emptyList();
    }

    private static List<ParameterSpec<?>> createParameterSpecsFromModelClass(Class<?> testModelClass) {
      return Arrays.stream(testModelClass.getMethods())
                   .filter(m -> m.isAnnotationPresent(JCUnitParameter.class))
                   .map(JCUnitTestExtension::toParameterSpec)
                   .collect(toList());
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

    private static TestTemplateInvocationContext toTestTemplateInvocationContext(Tuple testDataTuple) {
      return new TestTemplateInvocationContext() {
        @Override
        public List<Extension> getAdditionalExtensions() {
          return singletonList(toParameterResolver(testDataTuple));
        }
      };
    }

    private static ParameterResolver toParameterResolver(Tuple testDataTuple) {
      return new ParameterResolver() {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
          return parameterContext.getParameter().isAnnotationPresent(From.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
          return resolveValueOf(parameterName(parameterContext), testDataTuple);
        }
      };
    }

    private static String parameterName(ParameterContext parameterContext) {
      return parameterContext.getParameter().getAnnotation(From.class).value();
    }

    private static Object resolveValueOf(String sourceParameterName, Tuple testDataTuple) {
      return valueResolverFor(sourceParameterName, testDataTuple).resolve(testDataTuple);
    }

    private static ValueResolver<?> valueResolverFor(String sourceParameterName, Tuple testDataTuple) {
      if (!testDataTuple.containsKey(sourceParameterName))
        throw new ParameterResolutionException(String.format("Parameter '%s' not found. Available parameters are: %s", sourceParameterName, testDataTuple.keySet().stream().sorted().collect(toList())));
      return (ValueResolver<?>) (testDataTuple.get(sourceParameterName));
    }
  }

  private static <E> ParameterSpec<E> toParameterSpec(Method m) {
    return ParameterSpec.create(ValueResolvers.namedOf(m), ((List<ValueResolver<?>>) ValueResolvers.invoke(null, m)).toArray(new ValueResolver[0]));
  }
}