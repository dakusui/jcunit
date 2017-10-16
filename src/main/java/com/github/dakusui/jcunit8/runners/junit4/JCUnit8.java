package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.core.Utils;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;
import com.github.dakusui.jcunit8.pipeline.Config;
import com.github.dakusui.jcunit8.pipeline.Pipeline;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.*;
import com.github.dakusui.jcunit8.runners.junit4.utils.InternalUtils;
import com.github.dakusui.jcunit8.testsuite.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
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
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.core.Utils.createTestClassMock;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class JCUnit8 extends org.junit.runners.Parameterized {
  private final List<Runner> runners;
  private final TestSuite    testSuite;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass A test class
   */
  public JCUnit8(Class<?> klass) throws Throwable {
    super(klass);
    this.runners = createRunners(this.testSuite = buildTestSuite(
        getTestClass(),
        createParameterSpaceDefinitionTestClass(),
        getConfigFactory()
    ));
  }

  private static TestClassValidator[] createValidatorsFor(TestClass parameterSpaceDefinitionClass) {
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

  private static ParameterSpace buildParameterSpace(List<com.github.dakusui.jcunit8.factorspace.Parameter> parameters, List<Constraint> constraints) {
    return new ParameterSpace.Builder()
        .addAllParameters(parameters)
        .addAllConstraints(constraints)
        .build();
  }

  private static TestSuite buildTestSuite(Config config, ParameterSpace parameterSpace, TestScenario testScenario) {
    return Pipeline.Standard.<Tuple>create().execute(config, parameterSpace, testScenario);
  }

  private static SortedMap<String, com.github.dakusui.jcunit8.factorspace.Parameter> buildParameterMap(TestClass parameterSpaceDefinitionTestClass) {
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

  private static Function<Object, com.github.dakusui.jcunit8.factorspace.Parameter.Factory> buildParameterFactoryCreatorFrom(FrameworkMethod method) {
    return (Object o) -> {
      try {
        return (com.github.dakusui.jcunit8.factorspace.Parameter.Factory) method.invokeExplosively(o);
      } catch (Throwable throwable) {
        throw unexpectedByDesign(throwable);
      }
    };
  }

  /**
   * Mock {@code Parameterized} runner of JUnit 4.12.
   */
  @Override
  protected TestClass createTestClass(Class<?> testClass) {
    return createTestClassMock(super.createTestClass(testClass));
  }

  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    this.applyValidators(errors);
  }

  protected Statement withBeforeClasses(Statement statement) {
    return this.testSuite.getScenario().preSuiteProcedures().isEmpty() ?
        statement :
        InternalUtils.createRunBeforesForTestInput(
            statement,
            this.testSuite.getScenario().preSuiteProcedures(),
            Tuple.builder().put("@suite", this.testSuite).build()
        );
  }


  protected Statement withAfterClasses(Statement statement) {
    return this.testSuite.getScenario().preSuiteProcedures().isEmpty() ?
        statement :
        InternalUtils.createRunAftersForTestInput(
            statement,
            this.testSuite.getScenario().postSuiteProcedures(),
            Tuple.builder().put("@suite", this.testSuite).build()
        );
  }


  public static TestSuite buildTestSuite(
      TestClass testClass,
      TestClass parameterSpaceDefinitionTestClass,
      ConfigFactory configFactory
  ) {
    Collection<String> involvedParameterNames = InternalUtils.involvedParameters(testClass);
    return buildTestSuite(
        configFactory.create(),
        buildParameterSpace(
            new ArrayList<>(
                buildParameterMap(parameterSpaceDefinitionTestClass).values()
            ).stream(
            ).filter(
                parameter -> involvedParameterNames.contains(parameter.getName())
            ).collect(
                toList()
            ),
            NodeUtils.allTestPredicates(testClass).values().stream()
                .filter(each -> each instanceof Constraint)
                .map(Constraint.class::cast)
                .collect(toList())
        ),
        TestScenarioFactoryForJUnit4.create(testClass)
    );
  }

  private List<Runner> createRunners(TestSuite testSuite) {
    return IntStream.range(
        0, testSuite.size()
    ).mapToObj(
        i -> {
          try {
            return new TestCaseRunner(this.getTestClass().getJavaClass(), i, testSuite);
          } catch (InitializationError initializationError) {
            throw Checks.wrap(initializationError);
          }
        }
    ).collect(
        toList()
    );
  }

  private void applyValidators(List<Throwable> errors) {
    if (getTestClass().getJavaClass() != null) {
      for (TestClassValidator each : createValidatorsFor(createParameterSpaceDefinitionTestClass())) {
        errors.addAll(each.validateTestClass(getTestClass()));
      }
    }
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

  private static class TestCaseRunner extends ParentRunner<TestOracle> implements ITestCaseRunner {

    private final int       id;
    private final TestSuite testSuite;

    /**
     * Constructs a new {@code ParentRunner} that will run {@code @TestClass}
     *
     * @param javaClass A class that defines a test suite to be run.
     */
    private TestCaseRunner(Class<?> javaClass, int id, TestSuite testSuite) throws InitializationError {
      super(javaClass);
      this.id = id;
      this.testSuite = testSuite;
    }

    @Override
    protected String getName() {
      return format("[%d]", this.id);
    }

    @Override
    protected List<TestOracle> getChildren() {
      return testSuite.getScenario().oracles();
    }

    @Override
    protected Description describeChild(TestOracle child) {
      return Description.createTestDescription(
          getTestClass().getJavaClass(),
          String.format("%s[%s]", child.getName(), this.id),
          new Annotation[0]
      );
    }

    @Override
    protected void runChild(TestOracle child, RunNotifier notifier) {
      Description description = describeChild(child);

      TestCase testCase = this.testSuite.get(this.id);
      Tuple testInput = composeTestInput(testCase.getTestInput());
      if (child.shouldInvoke().test(testInput)) {
        runLeaf(oracleBlock(child, testInput), description, notifier);
      } else {
        notifier.fireTestIgnored(description);
      }
    }

    @Override
    protected void collectInitializationErrors(List<Throwable> errors) {
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

    private Statement withBeforeTestCases(Statement statement) {
      return testSuite.getScenario().preTestInputProcedures().isEmpty() ?
          statement :
          InternalUtils.createRunBeforesForTestInput(statement, testSuite.getScenario().preTestInputProcedures(), this.getTestCase().getTestInput());
    }

    private Statement withAfterTestCases(Statement statement) {
      List<FrameworkMethod> afters = getTestClass().getAnnotatedMethods(AfterTestCase.class);
      return afters.isEmpty() ? statement :
          InternalUtils.createRunAftersForTestInput(statement, testSuite.getScenario().postTestInputProcedures(), this.getTestCase().getTestInput());
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

    private Tuple composeTestInput(Tuple tuple) {
      try {
        return Tuple.builder()
            .putAll(tuple)
            .put("@ins", getTestClass().getOnlyConstructor().newInstance())
            .put("@suite", testSuite)
            .build();
      } catch (InstantiationException | InvocationTargetException | IllegalAccessException e) {
        throw Checks.wrap(e);
      }
    }

    private Statement oracleBlock(TestOracle testOracle, Tuple testInput) {
      Statement statement = oracleInvoker(testOracle, testInput);
      statement = withBeforesForTestOracle(testInput, statement);
      statement = withAftersForTestOracle(testInput, statement);
      return statement;
    }

    private Statement oracleInvoker(TestOracle oracle, Tuple testInput) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          oracle.accept(testInput);
        }
      };
    }

    private Statement withBeforesForTestOracle(Tuple testInput, Statement statement) {
      List<TupleConsumer> befores = testSuite.getScenario().preOracleProcedures();
      return befores.isEmpty() ?
          statement :
          new Statement() {
            @Override
            public void evaluate() throws Throwable {
              for (Consumer<Tuple> before : befores)
                before.accept(testInput);
              statement.evaluate();
            }
          };
    }

    private Statement withAftersForTestOracle(Tuple testInput, Statement statement) {
      List<TupleConsumer> afters = testSuite.getScenario().postOracleProcedures();
      return afters.isEmpty() ?
          statement :
          new Statement() {
            @Override
            public void evaluate() throws Throwable {
              statement.evaluate();
              for (Consumer<Tuple> after : afters)
                after.accept(testInput);
            }
          };
    }

    @Override
    public TestCase getTestCase() {
      return this.testSuite.get(this.id);
    }
  }
}