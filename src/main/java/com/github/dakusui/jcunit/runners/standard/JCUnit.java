package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorDef;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.coverage.Metrics;
import com.github.dakusui.jcunit.coverage.Report;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.Story;
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

import java.io.File;
import java.lang.annotation.Annotation;
import java.util.*;

public class JCUnit extends Parameterized {
  private final List<Runner> runners;

  private final TestSuite testSuite;
  private final RunnerContext runnerContext;

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    // To suppress unnecessary validation on @Parameters annotated methods,
    // We have overridden createTestClass method. For detail refer to the method.
    super(klass);
    this.runnerContext = new RunnerContext.Base(this.getTestClass().getJavaClass());

    List<FrameworkMethod> preconditionMethods = getTestClass().getAnnotatedMethods(Precondition.class);
    List<FrameworkMethod> customTestCaseMethods = getTestClass().getAnnotatedMethods(CustomTestCases.class);
    try {
      List<TestCase> testCases;
      ////
      // BEGIN: Plugin creation
      List<FactorDef> factorDefs = getFactorDefsFrom(getTestClass(), runnerContext);
      final Factors.Builder builder = new Factors.Builder();
      Utils.filter(factorDefs, new Utils.Predicate<FactorDef>() {
            @Override
            public boolean apply(FactorDef in) {
              in.addTo(builder);
              return true;
            }
      });
      runnerContext.setFactors(builder.build());
      final ConstraintChecker constraintChecker = new ConstraintChecker.Builder(getChecker(klass), runnerContext).build();
      runnerContext.setConstraintChecker(constraintChecker);
      final FactorSpace factorSpace = new FactorSpace.Builder()
          .addFactorDefs(factorDefs)
          .setTopLevelConstraintChecker(constraintChecker)
          .build();
      CoveringArrayEngine coveringArrayEngine = new CoveringArrayEngine.FromAnnotation(getGenerator(klass), runnerContext).build();
      // reporter creation must be done after other instances are created, because it depends on
      // factors and constraints. This isn't a good design, though... (FIXME)
      final List<Metrics<?>> metricsList = new LinkedList<Metrics<?>>();
      for (Reporter each : getReporters(klass)) {
        metricsList.add(new Metrics.Builder(each, runnerContext).build());
      }
      // validate constraint checker
      validateConstraintChecker(constraintChecker);
      // END: Plugin creation
      ////
      if (!SystemProperties.reuseTestSuite() || !IOUtils.determineTestSuiteFile(this.getTestClass().getJavaClass()).exists()) {
        testCases = generateTestCases(preconditionMethods, customTestCaseMethods, constraintChecker, factorSpace, coveringArrayEngine);
        ////
        // Create and hold a test suite object to use it in rules.
        this.testSuite = new TestSuite(testCases);
        if (SystemProperties.reuseTestSuite()) {
          saveTestCases(this.getTestClass().getJavaClass(), testCases);
        }
      } else {
        testCases = loadTestCases(this.getTestClass().getJavaClass());
        this.testSuite = new TestSuite(testCases);
      }
      final Map<FrameworkMethod, Set<Tuple>> coveredMethods = new HashMap<FrameworkMethod, Set<Tuple>>();
      this.runners = Utils.transform(
          this.testSuite,
          new Utils.Form<TestCase, Runner>() {

            @Override
            public Runner apply(TestCase in) {
              try {
                return new JCUnitRunner(
                    getTestClass().getJavaClass(),
                    factorSpace,
                    constraintChecker,
                    testSuite,
                    coveredMethods,
                    in);
              } catch (InitializationError initializationError) {
                throw Checks.wrap(initializationError);
              }
            }
          });
      ////
      // Issue-#10
      // process entire test suite by metrics objects whose targets are specified "All"
      for (Metrics each : metricsList) {
        //noinspection unchecked
        each.process(Utils.transform(testCases, new Utils.Form<TestCase, Tuple>() {
          @Override
          public Tuple apply(TestCase in) {
            return in.getTuple();
          }
        }));
      }
      for (Metrics each : metricsList) {
        new Report.Printer(System.out).submit(each);
      }
    } catch (JCUnitException e) {
      throw tryToRecreateRootCauseException(Checks.getRootCauseOf(e), e.getMessage());
    }
  }

  private static void validateConstraintChecker(ConstraintChecker constraintChecker) {
    String regex = "[A-Za-z0-9_]+";
    for (String eachTag : constraintChecker.getTags()) {
      String fqcn = constraintChecker.getClass().getCanonicalName();
      Checks.checkplugin(eachTag != null, "Constraint checker must not return null as a tag. (%s)", fqcn);
      Checks.checkplugin(eachTag.matches(regex), "A tag returned by constraint checker (%s) must match %s but not (%s)", eachTag, regex, fqcn);
    }
  }

  private List<TestCase> loadTestCases(Class<?> javaClass) {
    File testSuiteFile = IOUtils.determineTestSuiteFile(Checks.checknotnull(javaClass));
    List<?> ret = IOUtils.load(List.class, testSuiteFile);
    //noinspection unchecked
    return (List<TestCase>) ret;
  }

  private void saveTestCases(Class<?> javaClass, List<TestCase> testCases) {
    File testSuiteFile = IOUtils.determineTestSuiteFile(Checks.checknotnull(javaClass));
    if (!testSuiteFile.exists()) {
      IOUtils.mkdirs(testSuiteFile.getParentFile());
    }
    IOUtils.save(testCases, testSuiteFile);
  }


  public List<TestCase> generateTestCases(List<FrameworkMethod> preconditionMethods, List<FrameworkMethod> customTestCaseMethods, ConstraintChecker constraintChecker, final FactorSpace factorSpace, CoveringArrayEngine coveringArrayEngine) throws Throwable {
    ////
    // Generate a list of test cases using a specified tuple generator
    CoveringArray ca = coveringArrayEngine.generate(factorSpace);
    List<TestCase> testCases = Utils.newList();
    int id;
    for (id = ca.firstId(); id >= 0; id = ca.nextId(id)) {
      Tuple testCase = ca.get(id);
      if (shouldPerform(testCase, preconditionMethods)) {
        testCases.add(
            new TestCase(id, TestCase.Type.REGULAR, testCase
            ));
      }
    }
    // Skip to number of test cases generated.
    id = ca.size();
    ////
    // Compose a list of 'negative test cases' and register them.
    final List<Tuple> violations = constraintChecker.getViolations();
    id = registerTestCases(
        testCases,
        id,
        violations,
        TestCase.Type.VIOLATION,
        preconditionMethods);
    ////
    // Compose a list of 'custom test cases' and register them.
    registerTestCases(
        testCases,
        id,
        invokeCustomTestCasesMethod(customTestCaseMethods),
        TestCase.Type.CUSTOM,
        preconditionMethods);
    Checks.checkenv(testCases.size() > 0, "No test to be run was found.");
    return testCases;
  }

  public static Checker getChecker(Class<?> klass) {
    GenerateCoveringArrayWith generateWith = klass.getAnnotation(GenerateCoveringArrayWith.class);
    return generateWith == null
        ? Checker.Default.INSTANCE
        : generateWith.checker();
  }

  public static Generator getGenerator(Class<?> klass) {
    GenerateCoveringArrayWith annotation = klass.getAnnotation(GenerateCoveringArrayWith.class);
    return annotation == null
        ? Generator.Default.INSTANCE
        : annotation.engine();
  }

  public static Reporter[] getReporters(Class<?> klass) {
    GenerateCoveringArrayWith annotation = klass.getAnnotation(GenerateCoveringArrayWith.class);
    return annotation == null
        ? new Reporter[0]
        : annotation.reporters();
  }

  public static List<FactorDef> getFactorDefsFrom(Class c) {
    return getFactorDefsFrom(new TestClass(c), new RunnerContext.Base(c));
  }

  private static List<FactorDef> getFactorDefsFrom(TestClass testClass, RunnerContext runnerContext) {
    List<FactorDef> ret = Utils.newList();
    for (FrameworkField each : testClass.getAnnotatedFields(FactorField.class)) {
      ret.add(createFactorDefFrom(each, runnerContext));
    }
    return ret;
  }

  private static FactorDef createFactorDefFrom(FrameworkField field, RunnerContext runnerContext) {
    if (isSimpleFactorField(field)) {
      return new FactorDef.Simple(field.getName(), levelsProviderOf(field, runnerContext));
    }
    LevelsProvider levelsProvider = levelsProviderOf(field, runnerContext);
    int historyLength = 2;
    if (levelsProvider instanceof FSMLevelsProvider) {
      historyLength = ((FSMLevelsProvider) levelsProvider).historyLength();
    }
    //noinspection unchecked
    return new FactorDef.Fsm(
        field.getName(),
        FSMUtils.createFSM(field.getField()),
        Collections.<com.github.dakusui.jcunit.fsm.Parameters.LocalConstraintChecker>emptyList(),
        historyLength);
  }

  private static boolean isSimpleFactorField(FrameworkField frameworkField) {
    return !Story.class.isAssignableFrom(frameworkField.getType());
  }

  private static LevelsProvider levelsProviderOf(final FrameworkField field, RunnerContext runnerContext) {
    FactorField ann = field.getAnnotation(FactorField.class);
    LevelsProvider ret = new LevelsProvider.FromFactorField(field.getAnnotation(FactorField.class), runnerContext).build();
    if (ret instanceof FactorField.FactorFactory.Default.DummyLevelsProvider) {
      List<Object> values = FactorField.FactorFactory.Default.levelsGivenByUserDirectly(ann, runnerContext);
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
