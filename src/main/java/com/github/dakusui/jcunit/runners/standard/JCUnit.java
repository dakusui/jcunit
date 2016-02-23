package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArray;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.plugins.levelsproviders.SimpleLevelsProvider;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.core.TestCase;
import com.github.dakusui.jcunit.runners.core.TestSuite;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import org.junit.runner.Runner;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JCUnit extends Parameterized {
  private final List<Runner> runners;

  private final TestSuite testSuite;

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    // To suppress unnecessary validation on @Parameters annotated methods,
    // We have overridden createTestClass method. For detail refer to the method.
    super(klass);
    List<FrameworkMethod> preconditionMethods = getTestClass().getAnnotatedMethods(Precondition.class);
    List<FrameworkMethod> customTestCaseMethods = getTestClass().getAnnotatedMethods(CustomTestCases.class);
    RunnerContext runnerContext = new RunnerContext.Base(this.getTestClass().getJavaClass());
    ConstraintChecker constraintChecker = new ConstraintChecker.Builder(getChecker(klass), runnerContext).build();
    final FactorSpace factorSpace = new FactorSpace.Builder()
        .addFactorDefs(getFactorDefsFrom(getTestClass()))
        .setTopLevelConstraintChecker(constraintChecker)
        .build();
    try {
      ////
      // Generate a list of test cases using a specified tuple generator
      CoveringArrayEngine coveringArrayEngine = new CoveringArrayEngine.BuilderFromAnnotation(getGenerator(klass), runnerContext).build();
      CoveringArray ca = coveringArrayEngine.generate(factorSpace);
      List<TestCase> testCases = Utils.newList();
      int id;
      for (id = ca.firstId();
           id >= 0; id = ca.nextId(id)) {
        Tuple testCase = ca.get(id);
        if (shouldPerform(testCase, preconditionMethods)) {
          testCases.add(
              new TestCase(id, TestCase.Type.Generated, testCase
              ));
        }
      }
      // Skip to number of test cases generated.
      id = ca.size();
      ////
      // Compose a list of 'negative test cases' and register them.
      final ConstraintChecker cm = constraintChecker;
      final List<Tuple> violations = cm.getViolations();
      id = registerTestCases(
          testCases,
          id,
          violations,
          TestCase.Type.Violation,
          preconditionMethods);
      ////
      // Compose a list of 'custom test cases' and register them.
      registerTestCases(
          testCases,
          id,
          invokeCustomTestCasesMethod(customTestCaseMethods),
          TestCase.Type.Custom,
          preconditionMethods);
      Checks.checkenv(testCases.size() > 0, "No test to be run was found.");
      ////
      // Create and hold a test suite object to use it in rules.
      this.testSuite = new TestSuite(testCases);
      this.runners = Utils.transform(
          this.testSuite,
          new Utils.Form<TestCase, Runner>() {

            @Override
            public Runner apply(TestCase in) {
              try {
                return new JCUnitRunner(
                    getTestClass().getJavaClass(),
                    factorSpace,
                    cm,
                    testSuite,
                    in);
              } catch (InitializationError initializationError) {
                throw Checks.wrap(initializationError);
              }
            }
          });

    } catch (JCUnitException e) {
      throw tryToRecreateRootCauseException(Checks.getRootCauseOf(e), e.getMessage());
    }
  }

  public static Checker getChecker(Class<?> klass) {
    GenerateCoveringArrayWith generateWith = klass.getAnnotation(GenerateCoveringArrayWith.class);
    return generateWith == null
        ? Checker.DEFAULT
        : generateWith.checker();
  }

  public static Generator getGenerator(Class<?> klass) {
    GenerateCoveringArrayWith annotation = klass.getAnnotation(GenerateCoveringArrayWith.class);
    return annotation == null
        ? Generator.DEFAULT
        : annotation.engine();
  }

  public static List<FactorDef> getFactorDefsFrom(Class c) {
    return getFactorDefsFrom(new TestClass(c));
  }

  private static List<FactorDef> getFactorDefsFrom(TestClass testClass) {
    List<FactorDef> ret = Utils.newList();
    for (FrameworkField each : testClass.getAnnotatedFields(FactorField.class)) {
      ret.add(createFactorDefFrom(each));
    }
    return ret;
  }

  private static FactorDef createFactorDefFrom(FrameworkField field) {
    if (isSimpleFactorField(field)) {
      return new FactorDef.Simple(field.getName(), levelsProviderOf(field));
    }
    LevelsProvider levelsProvider = levelsProviderOf(field);
    int historyLength = 2;
    if (levelsProvider instanceof FSMLevelsProvider) {
      historyLength = ((FSMLevelsProvider)levelsProvider).historyLength();
    }
    Checks.checktest(levelsProvider instanceof FSMLevelsProvider, "");
    return new FactorDef.Fsm(field.getName(), createFSM(field.getField()), Collections.<com.github.dakusui.jcunit.fsm.Parameters.LocalConstraintChecker>emptyList(), historyLength);
  }

  private static boolean isSimpleFactorField(FrameworkField frameworkField) {
    return !Story.class.isAssignableFrom(frameworkField.getType());
  }

  /**
   * {@code f} Must be annotated with {@code FactorField}. Its {@code levelsProvider} must be an FSMLevelsProvider.
   * Typed with {@code Story} class.
   *
   * @param f              A field from which an FSM is created.
   * @return Created FSM object
   */
  public static FSM<Object> createFSM(Field f) {
    Checks.checknotnull(f);
    Class<?> clazz = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[1];
    //noinspection unchecked
    return JCUnit.createFSM(f.getName(), (Class<? extends FSMSpec<Object>>) clazz);
  }

  public static <SUT> FSM<SUT> createFSM(String fsmName, Class<? extends FSMSpec<SUT>> fsmSpecClass) {
    return new FSM.Base<SUT>(fsmName, fsmSpecClass);
  }

  private static LevelsProvider levelsProviderOf(final FrameworkField field) {
    FactorField ann = field.getAnnotation(FactorField.class);
    LevelsProvider ret = Plugins.levelsProviderOf(field.getAnnotation(FactorField.class));
    if (ret instanceof FactorField.FactorFactory.Default.DummyLevelsProvider) {
      List<Object> values = FactorField.FactorFactory.Default.levelsGivenByUserThroughImmediate(ann);
      if (values == null) {
        values = FactorField.DefaultLevels.defaultLevelsOf(field.getType());
      }
      final Object[] arr = values.toArray();
      ret = new SimpleLevelsProvider() {
        @Override
        protected Object[] values() {
          return arr;
        }
      };
    }
    return ret;
  }


  @Override
  protected List<Runner> getChildren() {
    return this.runners;
  }

  /**
   * Mock {@code Parameterized} runner of JUnit 4.12.
   */
  @Override
  protected TestClass createTestClass(Class<?> clazz) {
    return new TestClass(clazz) {
      public List<FrameworkMethod> getAnnotatedMethods(
          Class<? extends Annotation> annotationClass) {
        if (Parameterized.Parameters.class.equals(annotationClass)) {
          return Collections.singletonList(new FrameworkMethod(ReflectionUtils.getMethod(DummyMethodHolderForParameterizedRunner.class, "dummy")));

        }
        return super.getAnnotatedMethods(annotationClass);
      }
    };
  }

  private boolean shouldPerform(Tuple testCase, List<FrameworkMethod> preconditionMethods) {
    if (preconditionMethods.isEmpty()) {
      return true;
    }
    for (FrameworkMethod m : preconditionMethods) {
      try {
        Object testObject = TestCaseUtils.toTestObject(
            this.getTestClass().getJavaClass(),
            testCase);
        if ((Boolean) m.invokeExplosively(testObject)) {
          return true;
        }
      } catch (Throwable throwable) {
        throw Checks.wrap(throwable, "Failed to execute ");
      }
    }
    return false;
  }

  private int registerTestCases(
      List<TestCase> testCases,
      int id,
      Iterable<Tuple> testCaseTuplesToBeAdded,
      TestCase.Type type,
      List<FrameworkMethod> preconditionMethods)
      throws Throwable {
    for (Tuple testCase : testCaseTuplesToBeAdded) {
      if (shouldPerform(testCase, preconditionMethods)) {
        testCases.add(new TestCase(id, type, testCase));
      }
      id++;
    }
    return id;
  }

  private List<Tuple> invokeCustomTestCasesMethod(List<FrameworkMethod> customTestCasesMethods) {
    List<Tuple> ret = new LinkedList<Tuple>();
    for (FrameworkMethod each : customTestCasesMethods) {
      try {
        Object r = each.invokeExplosively(null);
        if (r instanceof Iterable) {
          for (Object o : (Iterable) r) {
            addTestCase(o, ret, each);
          }
        } else {
          addTestCase(r, ret, each);
        }
      } catch (Throwable throwable) {
        throw Checks.wrap(throwable, "Failed to execute '%s'.", each.getName());
      }
    }
    return ret;
  }

  /**
   * Add test case to {@code tupleList}.
   * It will be converted to a tuple, if necessary.
   *
   * @param testCase        A test case object. Type is unknown.
   * @param tupleList       A list to test case tuple to add to.
   * @param frameworkMethod A framework method from which {@code testCase} is returned.
   */
  private void addTestCase(Object testCase, List<Tuple> tupleList, FrameworkMethod frameworkMethod) {
    Checks.checknotnull(
        testCase,
        "null is returned (or contained in a collection returned) by '%s.%s' (in %s)",
        frameworkMethod.getDeclaringClass(),
        frameworkMethod.getName()
    );

    if (testCase instanceof Tuple) {
      tupleList.add((Tuple) testCase);
    } else if (getTestClass().getJavaClass().isAssignableFrom(testCase.getClass())) {
      tupleList.add(TestCaseUtils.toTestCase(testCase));
    } else {
      Checks.checkcond(
          false,
          "Unknown type object (%s) is returned by '%s' (in %s)",
          testCase,
          frameworkMethod.getName(),
          frameworkMethod.getDeclaringClass()
      );
    }
  }


  /**
   * A class referenced by createTestClass method.
   * This is only used to mock JUnit's Parameterized runner.
   */
  public static class DummyMethodHolderForParameterizedRunner {
    @SuppressWarnings("unused") // This method is referenced reflectively.
    @Parameters
    public static Object[][] dummy() {
      return new Object[][] { {} };
    }
  }

  private static Throwable tryToRecreateRootCauseException(Throwable rootCause, String message) {
    rootCause = Checks.checknotnull(rootCause);
    if (message == null)
      return rootCause;
    Throwable ret = null;
    try {
      ret = ReflectionUtils.create(rootCause.getClass(), new ReflectionUtils.TypedArg(String.class, message));
      ret.setStackTrace(rootCause.getStackTrace());
    } finally {
      if (ret == null)
        ret = rootCause;
    }
    return ret;
  }
}
