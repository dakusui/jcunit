package com.github.jcunit.runners.junit5;

import com.github.jcunit.annotations.*;
import com.github.jcunit.core.Invokable;
import com.github.jcunit.core.dnftree.NodeUtils;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.factorspace.Constraint;
import com.github.jcunit.factorspace.ParameterSpace;
import com.github.jcunit.factorspace.TuplePredicate;
import com.github.jcunit.model.ParameterSpaceSpec;
import com.github.jcunit.model.ParameterSpec;
import com.github.jcunit.model.ValueResolver;
import com.github.jcunit.pipeline.Pipeline;
import com.github.jcunit.pipeline.PipelineSpec;
import com.github.jcunit.testsuite.TestData;
import com.github.jcunit.utils.ReflectionUtils;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.jcunit.annotations.ConfigurePipelineWith.Utils.configurationOf;
import static com.github.jcunit.annotations.ConfigurePipelineWith.Utils.createPipelineSpecFrom;
import static com.github.jcunit.annotations.JCUnitCondition.Type.CONSTRAINT;
import static com.github.jcunit.runners.junit5.JCUnitTestEngine.Utils.createParameterSpaceSpec;
import static com.github.jcunit.runners.junit5.JCUnitTestEngine.Utils.definedPredicatesFrom;
import static com.github.jcunit.runners.junit5.JCUnitTestEngineUtils.*;
import static com.github.valid8j.fluent.Expectations.require;
import static com.github.valid8j.fluent.Expectations.value;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;
import static org.junit.jupiter.api.TestInstance.Lifecycle.PER_CLASS;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@TestInstance(PER_CLASS)
public class JCUnitTestEngine implements BeforeAllCallback, TestTemplateInvocationContextProvider {
  private final ExtensionContext.Namespace namespace = ExtensionContext.Namespace.create(JCUnitTestEngine.class);

  public JCUnitTestEngine() {
  }

  @Override
  public void beforeAll(ExtensionContext context) {
    {
      List<String> errors = new LinkedList<>();
      Class<?> testClass = context.getTestClass()
                                  .orElseThrow(AssertionError::new);
      ConfigurePipelineWith configAnnotation = configurationOf(testClass);
      PipelineSpec pipelineSpec = createPipelineSpecFrom(configAnnotation,
                                                         getArguments(configAnnotation),
                                                         getSeedGenerators(configAnnotation.parameterSpaceSpecClass()));
      Class<?> parameterSpaceSpecClass = getParameterSpaceSpecClass(testClass, configAnnotation);

      Map<String, List<Object>> namedEntitiesInParameterSpaceSpec = definedNamesInParameterSpace(parameterSpaceSpecClass);

      validateNoNameDuplications(errors, namedEntitiesInParameterSpaceSpec);
      namedMethodsFromParameterSpaceClass(parameterSpaceSpecClass).forEach(eachNamedMethod -> validateNamedMethod(errors,
                                                                                                                  eachNamedMethod));

      SortedMap<String, TuplePredicate> definedPredicates = definedPredicatesFrom(parameterSpaceSpecClass);
      ParameterSpaceSpec parameterSpaceSpec = createParameterSpaceSpec(
          Utils.createParameterSpecsFromModelClass(parameterSpaceSpecClass),
          definedPredicates.values()
                           .stream()
                           .filter(p -> p instanceof Constraint)
                           .map(p -> (Constraint) p)
                           .filter(Constraint::isExplicit)
                           .collect(toList()));
      Set<String> knownNames = namedEntitiesInParameterSpaceSpec.keySet();
      validateReferencesOfParameters(errors, parameterSpaceSpec, knownNames);
      validateReferencesOfConstraints(errors, parameterSpaceSpec, knownNames);
      require(value(errors).satisfies().empty());

      List<TestData> testDataSet = Utils.generateTestDataSet(pipelineSpec, parameterSpaceSpec.toParameterSpace(asList("param2", "param2")));
      context.getStore(namespace).put("testDataSet", testDataSet);
      context.getStore(namespace).put("parameterSpaceSpec", parameterSpaceSpec);
      context.getStore(namespace).put("definedPredicates", definedPredicates);
    }
  }

  private static Class<?> getParameterSpaceSpecClass(Class<?> testClass, ConfigurePipelineWith configure) {
    return (!configure.parameterSpaceSpecClass().equals(Object.class)) ? configure.parameterSpaceSpecClass()
                                                                       : testClass;
  }

  @Override
  public boolean supportsTestTemplate(ExtensionContext context) {
    return context.getTestMethod()
                  .filter(m -> m.isAnnotationPresent(JCUnitTest.class))
                  .isPresent();
  }

  @Override
  public Stream<TestTemplateInvocationContext> provideTestTemplateInvocationContexts(ExtensionContext context) {
    @SuppressWarnings("unchecked") List<TestData> testDataSet = context.getStore(namespace)
                                                                       .get("testDataSet", List.class);
    @SuppressWarnings("unchecked") SortedMap<String, TuplePredicate> definedPredicates = context.getStore(namespace)
                                                                                                .get("definedPredicates",
                                                                                                     SortedMap.class);
    List<String> referencedParameterNames = context.getTestMethod().map(m -> Arrays.stream(m.getParameters())
                                                                                   .filter(p -> p.isAnnotationPresent(From.class))
                                                                                   .map(a -> a.getAnnotation(From.class).value())
                                                                                   .collect(toList()))
                                                   .orElse(emptyList());
    Method testMethod = context.getTestMethod().orElseThrow(NoSuchElementException::new);
    return testDataSet.stream()
                      .filter((TestData each) -> satisfiesPrecondition(testMethod, each, definedPredicates))
                      .map((TestData each) -> Utils.toTestTemplateInvocationContext(each, referencedParameterNames));
  }

  private static boolean satisfiesPrecondition(Method method, TestData testData, SortedMap<String, TuplePredicate> definedPredicates) {
    if (!method.isAnnotationPresent(Given.class)) return testData.getCategory() == TestData.Category.REGULAR
                                                         || testData.getCategory() == TestData.Category.SEED;
    return NodeUtils.buildPredicate(method.getAnnotation(Given.class).value(),
                                    definedPredicates)
                    .test(testData.getTestDataTuple());
  }

  enum Utils {
    ;

    static ParameterSpaceSpec createParameterSpaceSpec(List<ParameterSpec<?>> parameterSpecsFromModelClass,
                                                       List<Constraint> constraintsFromModelClass) {
      return ParameterSpaceSpec.create(parameterSpecsFromModelClass, constraintsFromModelClass);
    }

    public static SortedMap<String, TuplePredicate> definedPredicatesFrom(Class<?> testClass) {
      SortedMap<String, TuplePredicate> definedPredicates = new TreeMap<>();
      Arrays.stream(testClass.getMethods())
            .filter(method -> method.isAnnotationPresent(JCUnitCondition.class))
            .map(method -> toTuplePredicate(method, method.getAnnotation(JCUnitCondition.class).value() == CONSTRAINT))
            .forEach(condition -> definedPredicates.put(condition.getName(), condition));
      return definedPredicates;
    }

    /**
     * `method` must be static and has annotation of {@link From}.
     *
     * @param method A method from which a `TuplePredicate` will be created.
     * @return A tuple predicate.
     */
    private static TuplePredicate toTuplePredicate(Method method, boolean isConstraint) {
      Invokable<Boolean> invokable = Invokable.from(null, method);
      Predicate<Tuple> tuplePredicate = tuple -> invokable.invoke(invokable.parameters()
                                                                           .stream()
                                                                           .map(p -> valueFromTupleForParameter(tuple, p))
                                                                           .map(v -> (ValueResolver<?>) v)
                                                                           .map(r -> r.resolve(tuple)).toArray());
      return isConstraint ? Constraint.create(nameOf(method),
                                              true,
                                              invokable.parameterNames(),
                                              tuplePredicate)
                          : TuplePredicate.of(nameOf(method),
                                              invokable.parameterNames(),
                                              tuplePredicate);
    }

    @SuppressWarnings("unchecked")
    private static Object valueFromTupleForParameter(Tuple tuple, Invokable.Parameter p) {
      return p.index() >= 0 ? ((List<ValueResolver<?>>) tuple.get(p.name())).get(p.index())
                            : tuple.get(p.name());
    }

    private static List<ParameterSpec<?>> createParameterSpecsFromModelClass(Class<?> testModelClass) {
      return Arrays.stream(testModelClass.getMethods())
                   .filter(m -> m.isAnnotationPresent(JCUnitParameter.class))
                   .map(Utils::toParameterSpec)
                   .collect(toList());
    }

    private static List<TestData> generateTestDataSet(PipelineSpec config, ParameterSpace parameterSpace) {
      return new ArrayList<>(new Pipeline.Standard(config).generateTestSuite(parameterSpace));
    }

    private static TestTemplateInvocationContext toTestTemplateInvocationContext(TestData testData, List<String> referencedParameterNames) {
      return new TestTemplateInvocationContext() {

        private final Tuple testDataTuple = testData.getTestDataTuple();

        @Override
        public String getDisplayName(int invocationIndex) {
          return String.format("%s:%s",
                               testData.getCategory(),
                               referencedParameterNames.stream()
                                                       .sorted()
                                                       .map(k -> String.format("%s:%s", k, formatTupleValue(k)))
                                                       .collect(Collectors.joining(",")));
        }

        @Override
        public List<Extension> getAdditionalExtensions() {
          return singletonList(toParameterResolver(testDataTuple));
        }

        private Object formatTupleValue(String k) {
          Object o = testDataTuple.get(k);
          return o instanceof List ? ((List<?>) o).size() == 1 ? ((List<?>) o).get(0)
                                                               : o
                                   : o;
        }
      };
    }

    private static ParameterResolver toParameterResolver(Tuple testDataTuple) {
      return new ParameterResolver() {
        @Override
        public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
          return parameterContext.getParameter()
                                 .isAnnotationPresent(From.class);
        }

        @Override
        public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
          int index = 0;
          if (parameterContext.getParameter().isAnnotationPresent(From.class))
            index = parameterContext.getParameter().getAnnotation(From.class).index();
          List<Object> arguments = resolveValueOf(parameterName(parameterContext), testDataTuple);
          if (index >= 0) return arguments.get(index);
          else return arguments;
        }
      };
    }

    private static String parameterName(ParameterContext parameterContext) {
      return parameterContext.getParameter().getAnnotation(From.class).value();
    }

    private static List<Object> resolveValueOf(String sourceParameterName, Tuple testDataTuple) {
      return valueResolversFor(sourceParameterName, testDataTuple).stream()
                                                                  .map(r -> r.resolve(testDataTuple))
                                                                  .collect(toList());
    }

    @SuppressWarnings("unchecked")
    private static List<ValueResolver<?>> valueResolversFor(String sourceParameterName, Tuple testDataTuple) {
      if (!testDataTuple.containsKey(sourceParameterName))
        throw new ParameterResolutionException(composeParameterResolutionErrorMessage(sourceParameterName,
                                                                                      testDataTuple));
      return (List<ValueResolver<?>>) (testDataTuple.get(sourceParameterName));
    }

    @SuppressWarnings("unchecked")
    private static <E> ParameterSpec<E> toParameterSpec(Method m) {
      return ParameterSpec.create(ValueResolver.nameOf(m),
                                  m.getAnnotation(JCUnitParameter.class).type(),
                                  m.getAnnotation(JCUnitParameter.class).args(),
                                  ((List<ValueResolver<?>>) ReflectionUtils.invoke(null, m)).toArray(new ValueResolver[0]));
    }

    private static String composeParameterResolutionErrorMessage(String sourceParameterName, Tuple testDataTuple) {
      return String.format("Parameter '%s' not found. Available parameters are: %s",
                           sourceParameterName,
                           testDataTuple.keySet()
                                        .stream()
                                        .sorted()
                                        .collect(toList()));
    }
  }
}