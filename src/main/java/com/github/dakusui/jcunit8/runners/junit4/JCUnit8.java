package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.exceptions.BaseException;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.runners.junit4.annotations.*;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith.ConfigFactory;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.Ignore;
import org.junit.internal.runners.statements.InvokeMethod;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.core.Utils.createTestClassMock;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static com.github.dakusui.jcunit8.factorspace.Parameter.Factory;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;

public class JCUnit8 extends org.junit.runners.Parameterized {
  private final SortedMap<String, TestPredicate> predicates;
  private final TestSuite<Tuple>                 testSuite;
  private final List<Runner>                     runners;

  public JCUnit8(Class<?> klass) throws Throwable {
    super(klass);
    try {
      ConfigFactory configFactory = getConfigFactory();
      Object parameterSpaceDefinition = createParameterSpaceDefinition();
      this.predicates = buildTestConstraintMap(parameterSpaceDefinition);
      this.testSuite = buildTestSuite(
          configFactory.create(),
          buildParameterSpace(
              new ArrayList<>(buildParameterMap(parameterSpaceDefinition).values()),
              this.predicates.values().stream()
                  .filter(each -> each instanceof Constraint)
                  .map(Constraint.class::cast)
                  .collect(toList())
          ));
      this.runners = createRunners();
    } catch (BaseException e) {
      if (e.getCause() instanceof InitializationError) {
        throw TestDefinitionException.wrap(e.getCause());
      }
      throw e;
    } catch (Throwable throwable) {
      throw TestDefinitionException.wrap(throwable);
    }
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    // TODO
    //    super.collectInitializationErrors(errors);
    this.validateParameterSourceMethods(errors);
  }

  private void validateParameterSourceMethods(List<Throwable> errors) {
    // TODO
  }


  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  /**
   * Mock {@code Parameterized} runner of JUnit 4.12.
   */
  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  private ConfigFactory getConfigFactory() {
    try {
      //noinspection unchecked
      return getConfigureWithAnnotation().value().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw unexpectedByDesign(e);
    }
  }

  private Object createParameterSpaceDefinition() {
    ConfigureWith configureWith = getConfigureWithAnnotation();
    Class parameterSpaceDefinitionClass = configureWith.parameterSpace();
    try {
      if (Objects.equals(parameterSpaceDefinitionClass, Object.class))
        parameterSpaceDefinitionClass = this.getTestClass().getJavaClass();
      return parameterSpaceDefinitionClass.newInstance();
    } catch (IllegalAccessException | InstantiationException e) {
      throw TestDefinitionException.testClassIsInvalid(parameterSpaceDefinitionClass);
    }
  }

  private ConfigureWith getConfigureWithAnnotation() {
    ConfigureWith ret = this.getTestClass().getAnnotation(ConfigureWith.class);
    if (ret == null)
      ret = ConfigureWith.DEFAULT_INSTANCE;
    return ret;
  }

  private ParameterSpace buildParameterSpace(List<com.github.dakusui.jcunit8.factorspace.Parameter> parameters, List<Constraint> constraints) {
    return new ParameterSpace.Builder()
        .addAllParameters(parameters)
        .addAllConstraints(constraints)
        .build();
  }

  private List<Runner> createRunners() {
    AtomicInteger i = new AtomicInteger(0);
    return this.testSuite.stream()
        .map((Function<TestCase<Tuple>, Runner>) tupleTestCase -> {
          try {
            return new MyBlockJUnit4ClassRunner(i.getAndIncrement(), tupleTestCase);
          } catch (InitializationError initializationError) {
            throw unexpectedByDesign(initializationError);
          }
        })
        .collect(toList());
  }

  private Optional<TestPredicate> lookupTestPredicate(String name) {
    return this.predicates.containsKey(name) ?
        Optional.of(this.predicates.get(name)) :
        Optional.empty();
  }

  private static SortedMap<String, com.github.dakusui.jcunit8.factorspace.Parameter> buildParameterMap(Object parameterSpaceDefinition) {
    return new TreeMap<String, com.github.dakusui.jcunit8.factorspace.Parameter>() {{
      new TestClass(parameterSpaceDefinition.getClass()).getAnnotatedMethods(ParameterSource.class).forEach(
          frameworkMethod -> put(frameworkMethod.getName(),
              buildParameterFactoryCreatorFrom(frameworkMethod)
                  .apply(parameterSpaceDefinition)
                  .create(frameworkMethod.getName())
          ));
    }};
  }

  private static SortedMap<String, TestPredicate> buildTestConstraintMap(Object parameterSpaceDefinition) {
    return new TreeMap<String, TestPredicate>() {
      {
        new TestClass(parameterSpaceDefinition.getClass()).getAnnotatedMethods(Condition.class).stream()
            .filter((FrameworkMethod frameworkMethod) -> frameworkMethod.getAnnotation(Condition.class).constraint())
            .forEach((FrameworkMethod frameworkMethod) -> put(
                frameworkMethod.getName(),
                frameworkMethod.getAnnotation(Condition.class).constraint() ?
                    buildConstraintCreatorFrom(frameworkMethod).apply(parameterSpaceDefinition) :
                    buildTestPredicateCreatorFrom(frameworkMethod).apply(parameterSpaceDefinition)
            ));
      }
    };
  }

  private static Function<Object, TestPredicate> buildTestPredicateCreatorFrom(FrameworkMethod method) {
    return o -> new TestPredicate() {
      @Override
      public boolean test(Tuple tuple) {
        try {
          return (boolean) method.invokeExplosively(
              o,
              involvedKeys().stream()
                  .map(tuple::get)
                  .toArray());
        } catch (Throwable e) {
          throw unexpectedByDesign(e);
        }
      }

      @Override
      public List<String> involvedKeys() {
        return getParameterAnnotationsFrom(method, From.class).stream()
            .map(From::value)
            .collect(toList());
      }

      @Override
      public String toString() {
        return method.getMethod().getName();
      }
    };
  }

  private static TestSuite<Tuple> buildTestSuite(Config<Tuple> config, ParameterSpace parameterSpace) {
    return Pipeline.Standard.<Tuple>create().execute(config, parameterSpace);
  }

  private static Function<Object, Constraint> buildConstraintCreatorFrom(FrameworkMethod method) {
    return o -> Constraint.fromCondition(buildTestPredicateCreatorFrom(method).apply(o));
  }

  private static Function<Object, com.github.dakusui.jcunit8.factorspace.Parameter.Factory> buildParameterFactoryCreatorFrom(FrameworkMethod method) {
    return (Object o) -> {
      try {
        return (Factory) method.invokeExplosively(o);
      } catch (Throwable throwable) {
        throw unexpectedByDesign(throwable);
      }
    };
  }

  private static <A extends Annotation> List<A> getParameterAnnotationsFrom(FrameworkMethod method, Class<A> annotationClass) {
    return Stream.of(method.getMethod().getParameterAnnotations())
        .map((Function<Annotation[], List<? extends Annotation>>) Arrays::asList)
        .map((List<? extends Annotation> annotations) -> {
          //noinspection unchecked
          return (A) annotations.stream()
              .filter((Annotation eachAnnotation) -> annotationClass.isAssignableFrom(eachAnnotation.getClass()))
              .findFirst().orElseThrow(RuntimeException::new);
        }).collect(toList());
  }

  /**
   * This method is only used through reflection to let JUnit know the test case is ignored since
   * no matching test method is defined for it.
   *
   * @see JCUnit8.MyBlockJUnit4ClassRunner#getDummyMethodForNoMatchingMethodFound()
   */
  @Ignore
  @SuppressWarnings({ "unused", "WeakerAccess" })
  public static void noMatchingTestMethodIsFoundForThisTestCase() {
  }


  private class MyBlockJUnit4ClassRunner extends BlockJUnit4ClassRunner {
    private final TestCase<Tuple> tupleTestCase;
    int id;

    MyBlockJUnit4ClassRunner(int id, TestCase<Tuple> tupleTestCase) throws InitializationError {
      super(JCUnit8.this.getTestClass().getJavaClass());
      this.tupleTestCase = tupleTestCase;
      this.id = id;
    }

    @Override
    protected String getName() {
      return String.format("[%d]", this.id);
    }

    @Override
    protected String testName(final FrameworkMethod method) {
      return String.format("%s[%d]", method.getName(), this.id);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
      validateZeroArgConstructor(errors);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
      // TODO
    }

    @Override
    protected Description describeChild(FrameworkMethod method) {
      return Description.createTestDescription(
          getTestClass().getJavaClass(),
          testName(method),
          method.getAnnotations()
      );
    }

    @Override
    public List<FrameworkMethod> getChildren() {
      try {
        List<FrameworkMethod> ret = new LinkedList<>();
        for (FrameworkMethod each : computeTestMethods()) {
          if (shouldInvoke(each, tupleTestCase.get()))
            ret.add(each);
        }
        if (ret.isEmpty())
          ret.add(getDummyMethodForNoMatchingMethodFound());
        return ret;
      } catch (Throwable t) {
        throw unexpectedByDesign(t);
      }
    }

    /**
     * Returns a {@link Statement} that invokes {@code method} on {@code test}
     */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
      return new InvokeMethod(method, test) {
        @Override
        public void evaluate() throws Throwable {
          Object[] args = getParameterAnnotationsFrom(method, From.class).stream()
              .map(From::value)
              .map(s -> tupleTestCase.get().get(s))
              .collect(toList())
              .toArray();
          method.invokeExplosively(test, args);
        }
      };
    }

    @Override
    public Object createTest() {
      try {
        return getTestClass().getOnlyConstructor().newInstance();
      } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
        throw unexpectedByDesign(e);
      }
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
      return new Annotation[0];
    }

    private boolean shouldInvoke(FrameworkMethod each, Tuple tuple) {
      ////
      // TODO: Should be memoized.
      //noinspection SimplifiableIfStatement
      if (each.getAnnotation(Given.class) == null)
        return true;
      return buildPredicate(each.getAnnotation(Given.class).value()).test(tuple);
    }

    private <T> Predicate<T> buildPredicate(String[] definition) {
      try {
        return Arrays.stream(definition)
            .map((String s) -> asList(s.split("&&")))
            .map(
                (List<String> terms) -> terms.stream()
                    .map((String s) -> {
                      if (Given.ALL_CONSTRAINTS.equals(s)) {
                        StringJoiner joiner = new StringJoiner("&&");
                        predicates.keySet().stream()
                            .filter(k -> predicates.get(k) instanceof Constraint)
                            .forEach(joiner::add);
                        return joiner.toString();
                      }
                      return s;
                    })
                    .map((String s) -> {
                      String name = s;
                      boolean negate = false;
                      if (s.startsWith("!")) {
                        negate = true;
                        name = s.substring(1);
                      }
                      TestPredicate predicate = lookupTestPredicate(name).orElseThrow(() -> unexpectedByDesign(s));
                      //noinspection unchecked
                      return !negate ?
                          (Predicate<T>) predicate :
                          (Predicate<T>) predicate.negate();

                    })
                    .reduce(Predicate::and)
                    .orElse((T t) -> false)
            )
            .reduce(Predicate::or)
            .orElse((T t) -> true);
      } catch (Exception e) {
        throw unexpectedByDesign(e);
      }
    }

    private FrameworkMethod getDummyMethodForNoMatchingMethodFound() {
      try {
        return new FrameworkMethod(JCUnit8.class.getMethod("noMatchingTestMethodIsFoundForThisTestCase")) {
          @Override
          public String getName() {

            return String.format("%s:%s", super.getName(), Objects.toString(tupleTestCase));
          }
        };
      } catch (NoSuchMethodException e) {
        throw unexpectedByDesign(e);
      }
    }
  }
}