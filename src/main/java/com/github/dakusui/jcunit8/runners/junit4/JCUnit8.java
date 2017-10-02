package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.*;
import com.github.dakusui.jcunit8.runners.junit4.utils.InternalUtils;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestScenario;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.*;
import org.junit.internal.runners.rules.RuleMemberValidator;
import org.junit.internal.runners.statements.RunAfters;
import org.junit.internal.runners.statements.RunBefores;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationsValidator;
import org.junit.validator.PublicClassValidator;
import org.junit.validator.TestClassValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.core.Utils.createTestClassMock;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static com.github.dakusui.jcunit8.exceptions.TestDefinitionException.parameterWithoutAnnotation;
import static com.github.dakusui.jcunit8.factorspace.Parameter.Factory;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class JCUnit8 extends org.junit.runners.Parameterized {

  private final TestSuite    testSuite;
  private final List<Runner> runners;

  public JCUnit8(Class<?> klass) throws Throwable {
    super(klass);
    ConfigFactory configFactory = getConfigFactory();
    TestClass parameterSpaceDefinition = createParameterSpaceDefinitionTestClass();
    Collection<String> involvedParameterNames = InternalUtils.involvedParameters(new TestClass(klass));
    this.testSuite = buildTestSuite(
        configFactory.create(),
        buildParameterSpace(
            new ArrayList<>(
                buildParameterMap(parameterSpaceDefinition).values()
            ).stream(
            ).filter(
                parameter -> involvedParameterNames.contains(parameter.getName())
            ).collect(
                toList()
            ),
            NodeUtils.allTestPredicates(getTestClass()).values().stream()
                .filter(each -> each instanceof Constraint)
                .map(Constraint.class::cast)
                .collect(toList())
        ),
        buildTestScenario(new TestClass(klass))
    );
    this.runners = createRunners();
  }

  private TestScenario buildTestScenario(TestClass testClass) {
    return new TestScenario.Builder().build();
  }

  private static Predicate<Tuple> shouldInvoke(FrameworkMethod method, SortedMap<String, TestPredicate> predicates) {
    return tuple -> {
      //noinspection SimplifiableIfStatement
      if (method.getAnnotation(Given.class) == null)
        return true;
      return NodeUtils.buildPredicate(
          method.getAnnotation(Given.class).value(),
          predicates
      ).test(
          tuple
      );
    };
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    this.applyValidators(errors);
  }

  private void applyValidators(List<Throwable> errors) {
    if (getTestClass().getJavaClass() != null) {
      for (TestClassValidator each : createValidatorsFor(createParameterSpaceDefinitionTestClass())) {
        errors.addAll(each.validateTestClass(getTestClass()));
      }
    }
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

  static TestClassValidator[] createValidatorsFor(TestClass parameterSpaceDefinitionClass) {
    return new TestClassValidator[] {
        new AnnotationsValidator(),
        new PublicClassValidator(),
        new TestClassValidator() {
          @Override
          public List<Exception> validateTestClass(TestClass testClass) {
            return new LinkedList<Exception>() {
              {
                validateFromAnnotationsAreReferencingExistingParameterSourceMethods(BeforeTestCase.class, testClass, this);
                validateFromAnnotationsAreReferencingExistingParameterSourceMethods(Before.class, testClass, this);
                validateFromAnnotationsAreReferencingExistingParameterSourceMethods(Test.class, testClass, this);
                validateFromAnnotationsAreReferencingExistingParameterSourceMethods(After.class, testClass, this);
                validateFromAnnotationsAreReferencingExistingParameterSourceMethods(AfterTestCase.class, testClass, this);
                validateAtLeastOneTestMethod(testClass, this);
              }

            };
          }

          private void validateAtLeastOneTestMethod(TestClass testClass, LinkedList<Exception> errors) {
            if (testClass.getAnnotatedMethods(Test.class).isEmpty()) {
              errors.add(new Exception("No runnable methods"));
            }
          }

          private void validateFromAnnotationsAreReferencingExistingParameterSourceMethods(Class<? extends Annotation> ann, TestClass testClass, List<Exception> errors) {
            testClass.getAnnotatedMethods(ann)
                .forEach(
                    frameworkMethod -> Stream.of(frameworkMethod.getMethod().getParameterAnnotations())
                        .forEach((Annotation[] annotations) -> Stream.of(annotations)
                            .filter((Annotation annotation) -> annotation instanceof From)
                            .forEach((Annotation annotation) -> {
                              List<FrameworkMethod> methods = parameterSpaceDefinitionClass.getAnnotatedMethods(ParameterSource.class).stream()
                                  .filter(
                                      (FrameworkMethod each) ->
                                          Objects.equals(each.getName(), From.class.cast(annotation).value()))
                                  .collect(toList());
                              if (methods.isEmpty())
                                errors.add(new Exception(
                                    format(
                                        "A method '%s' annotated with '%s' is not defined in '%s'",
                                        From.class.cast(annotation).value(),
                                        ParameterSource.class.getSimpleName(),
                                        parameterSpaceDefinitionClass.getJavaClass().getCanonicalName()
                                    )));
                            })));
          }
        }
    };
  }

  private ConfigFactory getConfigFactory() {
    try {
      return getConfigureWithAnnotation().value().newInstance();
    } catch (InstantiationException | IllegalAccessException e) {
      throw TestDefinitionException.wrap(e);
    }
  }

  private TestClass createParameterSpaceDefinitionTestClass() {
    Class parameterSpaceClass = getConfigureWithAnnotation().parameterSpace();
    return Objects.equals(parameterSpaceClass, ConfigureWith.DEFAULT_INSTANCE.parameterSpace()) ?
        this.getTestClass() :
        new TestClass(parameterSpaceClass);
  }

  private ConfigureWith getConfigureWithAnnotation() {
    ConfigureWith ret = this.getTestClass().getAnnotation(ConfigureWith.class);
    if (ret == null)
      ret = ConfigureWith.DEFAULT_INSTANCE;
    return ret;
  }

  static ParameterSpace buildParameterSpace(List<com.github.dakusui.jcunit8.factorspace.Parameter> parameters, List<Constraint> constraints) {
    return new ParameterSpace.Builder()
        .addAllParameters(parameters)
        .addAllConstraints(constraints)
        .build();
  }

  private List<Runner> createRunners() {
    AtomicInteger i = new AtomicInteger(0);
    return this.testSuite.stream()
        .map((Function<TestCase, Runner>) tupleTestCase -> {
          try {
            return new TestCaseRunner(i.getAndIncrement(), tupleTestCase, NodeUtils.allTestPredicates(getTestClass()));
          } catch (InitializationError initializationError) {
            throw unexpectedByDesign(formatInitializationErrorMessage(initializationError));
          }
        })
        .collect(toList());
  }

  private static String formatInitializationErrorMessage(InitializationError e) {
    return e.getCauses().stream().map(Throwable::getMessage).collect(Collectors.joining());
  }

  static SortedMap<String, com.github.dakusui.jcunit8.factorspace.Parameter> buildParameterMap(TestClass parameterSpaceDefinitionTestClass) {
    return new TreeMap<String, com.github.dakusui.jcunit8.factorspace.Parameter>() {
      {
        parameterSpaceDefinitionTestClass.getAnnotatedMethods(ParameterSource.class).forEach(
            frameworkMethod -> put(frameworkMethod.getName(),
                buildParameterFactoryCreatorFrom(frameworkMethod)
                    .apply(Utils.createInstanceOf(parameterSpaceDefinitionTestClass))
                    .create(frameworkMethod.getName())
            ));
      }
    };
  }

  static TestSuite buildTestSuite(Config config, ParameterSpace parameterSpace, TestScenario testScenario) {
    return Pipeline.Standard.<Tuple>create().execute(config, parameterSpace, testScenario);
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

  @SuppressWarnings("unchecked")
  public static <A extends Annotation> List<A> getParameterAnnotationsFrom(FrameworkMethod method, Class<A> annotationClass) {
    return Stream.of(method.getMethod().getParameterAnnotations())
        .map((Function<Annotation[], List<? extends Annotation>>) Arrays::asList)
        .map(
            (List<? extends Annotation> annotations) ->
                (A) annotations.stream(

                ).filter(
                    (Annotation eachAnnotation) -> annotationClass.isAssignableFrom(eachAnnotation.getClass())
                ).findFirst(

                ).orElseThrow(
                    () -> parameterWithoutAnnotation(
                        format(
                            "%s.%s",
                            method.getDeclaringClass().getCanonicalName(),
                            method.getName()
                        )))
        ).collect(toList());
  }

  /**
   * This method is only used through reflection to let JUnit know the test case is ignored since
   * no matching test method is defined for it.
   *
   * @see TestCaseRunner#getDummyMethodForNoMatchingMethodFound()
   */
  @Ignore
  @SuppressWarnings({ "unused", "WeakerAccess" })
  public static void noMatchingTestMethodIsFoundForThisTestCase() {
  }


  public class TestCaseRunner extends BlockJUnit4ClassRunner {
    private final TestCase tupleTestCase;
    int id;
    private final SortedMap<String, TestPredicate> predicates;

    TestCaseRunner(int id, TestCase tupleTestCase, SortedMap<String, TestPredicate> predicates) throws InitializationError {
      super(JCUnit8.this.getTestClass().getJavaClass());
      this.tupleTestCase = tupleTestCase;
      this.id = id;
      this.predicates = predicates;
    }

    @Override
    protected String getName() {
      return format("[%d]", this.id);
    }

    @Override
    protected String testName(final FrameworkMethod method) {
      return format("%s[%d]", method.getName(), this.id);
    }

    @Override
    protected void validateConstructor(List<Throwable> errors) {
      validateZeroArgConstructor(errors);
    }

    @Override
    protected void validateTestMethods(List<Throwable> errors) {
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

    public TestCase getTestCase() {
      return tupleTestCase;
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
      validatePublicVoidNoArgMethods(BeforeClass.class, true, errors);
      validatePublicVoidNoArgMethods(AfterClass.class, true, errors);
      RuleMemberValidator.CLASS_RULE_VALIDATOR.validate(getTestClass(), errors);
      RuleMemberValidator.CLASS_RULE_METHOD_VALIDATOR.validate(getTestClass(), errors);
      applyValidators(errors);
    }

    @Override
    protected Statement classBlock(final RunNotifier notifier) {
      Statement statement = childrenInvoker(notifier);
      if (!checkIfAllChildrenAreIgnored()) {
        statement = withBeforeTestCases(statement);
        statement = withAfterTestCases(statement);
      }
      return statement;
    }

    @Override
    protected Statement withBefores(FrameworkMethod method, Object target,
        Statement statement) {
      List<FrameworkMethod> befores = getTestClass().getAnnotatedMethods(Before.class).stream(
      ).map(
          InternalUtils.frameworkMethodInvokingArgumentsFromTestCase(this, target)
      ).collect(
          toList()
      );
      return befores.isEmpty() ?
          statement :
          new RunBefores(statement, befores, target);
    }

    @Override
    protected Statement withAfters(FrameworkMethod method, Object target,
        Statement statement) {
      List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(After.class).stream(
      ).map(
          InternalUtils.frameworkMethodInvokingArgumentsFromTestCase(this, target)
      ).collect(
          toList()
      );
      return afters.isEmpty() ?
          statement :
          new RunAfters(statement, afters, target);
    }

    /**
     * Returns a {@link Statement} that invokes {@code method} on {@code test}
     */
    @Override
    protected Statement methodInvoker(final FrameworkMethod method, final Object test) {
      try {
        return InternalUtils.createMethodInvoker(method, this, test);
      } catch (Throwable throwable) {
        throw new Error(throwable);
      }
    }

    @Override
    public Object createTest() {
      return Utils.createInstanceOf(getTestClass());
    }

    @Override
    protected Annotation[] getRunnerAnnotations() {
      return new Annotation[0];
    }

    private boolean shouldInvoke(FrameworkMethod method, Tuple tuple) {
      return shouldInvoke(method).test(tuple);
    }

    private Predicate<Tuple> shouldInvoke(FrameworkMethod method) {
      return tuple -> JCUnit8.shouldInvoke(method, predicates).test(tuple);
    }

    private FrameworkMethod getDummyMethodForNoMatchingMethodFound() {
      try {
        return new FrameworkMethod(JCUnit8.class.getMethod("noMatchingTestMethodIsFoundForThisTestCase")) {
          @Override
          public String getName() {

            return format("%s:%s", super.getName(), Objects.toString(tupleTestCase));
          }
        };
      } catch (NoSuchMethodException e) {
        throw unexpectedByDesign(e);
      }
    }

    private Statement withBeforeTestCases(Statement statement) {
      List<FrameworkMethod> befores = JCUnit8.this.getTestClass().getAnnotatedMethods(BeforeTestCase.class);
      return befores.isEmpty() ? statement :
          InternalUtils.createRunBeforesForTestCase(statement, befores, this);
    }

    private Statement withAfterTestCases(Statement statement) {
      List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterTestCase.class);
      return afters.isEmpty() ? statement :
          InternalUtils.createRunAftersForTestCase(statement, afters, this);
    }

    private boolean checkIfAllChildrenAreIgnored() {
      try {
        Method m = ParentRunner.class.getDeclaredMethod("areAllChildrenIgnored");
        boolean wasAccessible = m.isAccessible();
        m.setAccessible(true);
        try {
          return (boolean) m.invoke(this);
        } finally {
          m.setAccessible(wasAccessible);
        }
      } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
        throw new Error(e);
      }
    }
  }

}