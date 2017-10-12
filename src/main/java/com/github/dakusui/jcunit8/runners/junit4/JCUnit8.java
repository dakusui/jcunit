package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.AfterTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.BeforeTestCase;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.utils.InternalUtils;
import com.github.dakusui.jcunit8.testsuite.TestCase;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.*;
import org.junit.internal.runners.rules.RuleMemberValidator;
import org.junit.internal.runners.statements.InvokeMethod;
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
import org.junit.validator.TestClassValidator;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.dakusui.jcunit8.core.Utils.createTestClassMock;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
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
    this.testSuite = JCUnit8X.buildTestSuite(
        configFactory.create(),
        JCUnit8X.buildParameterSpace(
            new ArrayList<>(
                JCUnit8X.buildParameterMap(parameterSpaceDefinition).values()
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
        TestScenarioFactoryForJUnit4.create(new TestClass(klass))
    );
    this.runners = createRunners();
  }

  public static Statement createMethodInvoker(FrameworkMethod method, TestCaseRunner testCaseRunner, Object test) throws Throwable {
    return new InvokeMethod(method, test) {
      @Override
      public void evaluate() throws Throwable {
        InternalUtils.invokeExplosivelyWithArgumentsFromTestInput(method, testCaseRunner.getTestCase().getTestInput());
      }
    };
  }


  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    this.applyValidators(errors);
  }

  private void applyValidators(List<Throwable> errors) {
    if (getTestClass().getJavaClass() != null) {
      for (TestClassValidator each : JCUnit8X.createValidatorsFor(createParameterSpaceDefinitionTestClass())) {
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


  public class TestCaseRunner extends BlockJUnit4ClassRunner implements ITestCaseRunner {
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
          if (shouldInvoke(each, tupleTestCase.getTestInput()))
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
          InternalUtils.frameworkMethodInvokingArgumentsFromTestCase_(this, target)
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
          InternalUtils.frameworkMethodInvokingArgumentsFromTestCase_(this, target)
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
        return createMethodInvoker(method, this, test);
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
      return tuple -> InternalUtils.shouldInvoke(method, predicates).test(tuple);
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
          InternalUtils.createRunBeforesForTestCase_(statement, befores, this);
    }

    private Statement withAfterTestCases(Statement statement) {
      List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterTestCase.class);
      return afters.isEmpty() ? statement :
          InternalUtils.createRunAftersForTestCase_(statement, afters, this);
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