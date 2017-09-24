package com.github.dakusui.jcunit8.runners.junit4;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit8.exceptions.TestDefinitionException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.pipeline.stages.ConfigFactory;
import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ConfigureWith;
import com.github.dakusui.jcunit8.runners.junit4.utils.InternalUtils;
import com.github.dakusui.jcunit8.testsuite.TestOracle;
import com.github.dakusui.jcunit8.testsuite.TestSuite;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.ParentRunner;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;
import org.junit.runners.model.TestClass;
import org.junit.validator.TestClassValidator;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.IntStream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

public class JCUnit8X extends org.junit.runners.Parameterized {
  private final List<Runner> runners;

  /**
   * Only called reflectively. Do not use programmatically.
   *
   * @param klass
   */
  public JCUnit8X(Class<?> klass) throws Throwable {
    super(klass);
    this.runners = createRunners(buildTestSuite());
  }

  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  @Override
  protected void collectInitializationErrors(List<Throwable> errors) {
    this.applyValidators(errors);
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

  private TestSuite buildTestSuite() {
    Collection<String> involvedParameterNames = InternalUtils.involvedParameters(getTestClass());
    return JCUnit8.buildTestSuite(
        getConfigFactory().create(),
        JCUnit8.buildParameterSpace(
            new ArrayList<>(
                JCUnit8.buildParameterMap(createParameterSpaceDefinitionTestClass()).values()
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
        createTestOracles()
    );
  }

  private List<TestOracle> createTestOracles() {
    return null;
  }

  private void applyValidators(List<Throwable> errors) {
    if (getTestClass().getJavaClass() != null) {
      for (TestClassValidator each : JCUnit8.createValidatorsFor(createParameterSpaceDefinitionTestClass())) {
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

  private static class TestCaseRunner extends ParentRunner<TestOracle> {

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
      return testSuite.getTestOracles();
    }

    @Override
    protected Description describeChild(TestOracle child) {
      return Description.createTestDescription(
          getTestClass().getJavaClass(),
          child.getName(),
          child instanceof TestOracleForJUnit4 ?
              ((TestOracleForJUnit4) child).annotations() :
              new Annotation[0]
      );
    }

    @Override
    protected void runChild(TestOracle child, RunNotifier notifier) {
      Description description = describeChild(child);

      Tuple testCaseTuple = this.testSuite.get(this.id).get();
      if (!child.shouldInvoke(testCaseTuple)) {
        notifier.fireTestIgnored(description);
      } else {
        runLeaf(oracleBlock(child, testCaseTuple), description, notifier);
      }
    }

    private Statement oracleBlock(TestOracle testOracle, Tuple testCaseTuple) {
      Statement statement = oracleInvoker(testOracle, testCaseTuple);
      statement = withBeforesForTestOracle(testCaseTuple, statement);
      statement = withAftersForTestOracle(testCaseTuple, statement);
      return statement;
    }

    private Statement oracleInvoker(TestOracle oracle, Tuple testCaseTuple) {
      return new Statement() {
        @Override
        public void evaluate() throws Throwable {
          oracle.accept(testCaseTuple);
        }
      };
    }

    private Statement withBeforesForTestOracle(Tuple testCaseTuple, Statement statement) {
      List<Consumer<Tuple>> befores = testSuite.beforeTestOracle();
      return befores.isEmpty() ?
          statement :
          new Statement() {
            @Override
            public void evaluate() throws Throwable {
              for (Consumer<Tuple> before : befores)
                before.accept(testCaseTuple);
              statement.evaluate();
            }
          };
    }

    private Statement withAftersForTestOracle(Tuple testCaseTuple, Statement statement) {
      List<Consumer<Tuple>> befores = testSuite.beforeTestOracle();
      return befores.isEmpty() ?
          statement :
          new Statement() {
            @Override
            public void evaluate() throws Throwable {
              for (Consumer<Tuple> before : befores)
                before.accept(testCaseTuple);
              statement.evaluate();
            }
          };
    }
  }
}

