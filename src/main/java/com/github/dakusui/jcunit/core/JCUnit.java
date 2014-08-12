package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.JCUnitUserException;
import com.github.dakusui.jcunit.generators.TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGeneratorFactory;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;

import java.io.Serializable;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

public class JCUnit extends Suite {
  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    try {
      List<String> frameworkMethodFailures = new LinkedList<String>();
      List<FrameworkMethod> filterMethods = getFrameworkMethods(frameworkMethodFailures, FrameworkMethodValidator.TESTCASE_FILTER);
      if (filterMethods.size() > 1)
        frameworkMethodFailures.add(
            String.format(
                "Currently at most only one '@Filter' method can be defined. But %d found. %s",
                filterMethods.size(),
                filterMethods
            ));
      List<FrameworkMethod> customTestCaseMethods = getFrameworkMethods(frameworkMethodFailures, FrameworkMethodValidator.CUSTOM_TESTCASES);
      FrameworkMethod filterMethod = null;
      if (filterMethods.size() > 0) {
        filterMethod = filterMethods.get(0);
      }
      ////
      // Check if any error was found.
      ConfigUtils.checkEnv(frameworkMethodFailures.isEmpty(), "Errors are found in test class '%s':%s", getTestClass().getJavaClass().getCanonicalName(), frameworkMethodFailures);

      ////
      // Generate a list of test cases using a specified tuple generator
      TupleGenerator tupleGenerator = TupleGeneratorFactory.INSTANCE
          .createTupleGeneratorFromClass(klass);
      Factors factors = tupleGenerator.getFactors();
      int id;
      List<Serializable> labelsForGeneratedTestCase = new LinkedList<Serializable>();
      for (id = (int) tupleGenerator.firstId();
           id >= 0; id = (int) tupleGenerator.nextId(id)) {
        Tuple testCase = tupleGenerator.get(id);
        if (filterMethod == null || (Boolean) filterMethod.invokeExplosively(null, testCase)) {
          runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
              id, TestCaseType.Generated, labelsForGeneratedTestCase,
              factors,
              testCase));
        }
      }
      // Skip to number of test cases generated.
      id = (int) tupleGenerator.size();
      ////
      // Compose a list of 'negative test cases' and register them.
      ConstraintManager cm = tupleGenerator.getConstraintManager();
      final List<LabeledTestCase> violations = cm.getViolations();
      id = registerLabeledTestCases(
          id,
          factors,
          violations,
          TestCaseType.Violation,
          filterMethod);
      ////
      // Compose a list of 'custom test cases' and register them.
      registerLabeledTestCases(
          id,
          factors,
          invokeCustomTestCasesMethod(customTestCaseMethods),
          TestCaseType.Custom,
          filterMethod);
      ConfigUtils.checkEnv(runners.size() > 0, "No test to be run was found.");
    } catch (JCUnitUserException e) {
      e.setTargetClass(klass);
      throw e;
    }
  }

  private int registerLabeledTestCases(int id,
      Factors factors,
      Iterable<LabeledTestCase> labeledTestCases,
      TestCaseType testCaseType,
      FrameworkMethod filterMethod)
      throws Throwable {
    for (LabeledTestCase labeledTestCase : labeledTestCases) {
      if (filterMethod == null || (Boolean) filterMethod.invokeExplosively(null, labeledTestCase.getTestCase()))
        runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
            id, testCaseType, labeledTestCase.getLabels(),
            factors,
            labeledTestCase.getTestCase()));
      id++;
    }
    return id;
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  private List<LabeledTestCase> invokeCustomTestCasesMethod(List<FrameworkMethod> customTestCasesMethods) {
    List<LabeledTestCase> ret = new LinkedList<LabeledTestCase>();
    try {
      for (FrameworkMethod m : customTestCasesMethods) {
        Object r = m.invokeExplosively(null);
        if (r instanceof LabeledTestCase) {
          ret.add((LabeledTestCase) r);
        } else if (r instanceof Iterable) {
          for (Object o : (Iterable) r) {
            if (o instanceof LabeledTestCase) {
              ret.add((LabeledTestCase) o);
            } else {
              ConfigUtils.checkEnv(false, "Returned value of '%s' must contain only LabeledTestCase objects.");
            }
          }
        } else {
          Utils.checkcond(false);
        }
      }
    } catch (Throwable throwable) {
      Utils.rethrow(throwable, "Failed to execute '%s'.: (%s)", throwable.getMessage());
    }
    return ret;
  }

  private List<FrameworkMethod> getFrameworkMethods(List<String> failures, FrameworkMethodValidator validator) {
    Class<? extends Annotation> annClass = validator.getAnnotation();
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(annClass);
    List<FrameworkMethod> ret = new ArrayList<FrameworkMethod>(methods.size());
    for (FrameworkMethod m : methods) {
      if (validator.validate(m)) {
        ret.add(m);
      } else {
        failures.add(
            String.format(
                "Method '%s'(annotated by '%s') must satisfy the following conditions: %s",
                m.getName(),
                annClass.getName(),
                validator.getDescription()
            ));
      }
    }
    return ret;
  }


  public static interface FrameworkMethodValidator {
    public static final FrameworkMethodValidator CUSTOM_TESTCASES = new FrameworkMethodValidator() {
      @Override
      public boolean validate(FrameworkMethod m) {
        Method mm = m.getMethod();
        return m.isPublic() && m.isStatic() && mm.getParameterTypes().length == 0 &&
            (LabeledTestCase.class.isAssignableFrom(mm.getReturnType()) ||
                Iterable.class.isAssignableFrom(mm.getReturnType()));
      }

      @Override
      public Class<? extends Annotation> getAnnotation() {
        return CustomTestCases.class;
      }

      @Override
      public String getDescription() {
        return "public, static, no parameter, and returning 'LabeledTestCase' or an iterable of it";
      }
    };
    public static final FrameworkMethodValidator TESTCASE_FILTER  = new FrameworkMethodValidator() {
      @Override
      public boolean validate(FrameworkMethod m) {
        Method mm = m.getMethod();
        boolean ret = Boolean.TYPE.isAssignableFrom(mm.getReturnType());
        ret &= mm.getParameterTypes().length > 0;
        ret &= Tuple.class.isAssignableFrom(mm.getParameterTypes()[0]);
        return m.isStatic() && m.isPublic() && ret;
      }

      @Override
      public Class<? extends Annotation> getAnnotation() {
        return Filter.class;
      }

      @Override
      public String getDescription() {
        return "public, static, returning boolean, parameters are ";
      }
    };

    public boolean validate(FrameworkMethod m);

    public Class<? extends Annotation> getAnnotation();

    public String getDescription();
  }

  public static class TestCaseInternalAnnotation implements Annotation {

    private final TestCaseType       type;
    private final List<Serializable> labels;
    private final int                id;
    private       Factors            factors;
    private       Tuple              testCase;

    public TestCaseInternalAnnotation(int id, TestCaseType type,
        List<Serializable> labels, Factors factors, Tuple testCase) {
      Utils.checknotnull(type);
      Utils.checknotnull(labels);
      this.id = id;
      this.type = type;
      this.labels = labels;
      this.factors = factors;
      this.testCase = testCase;
    }

    @Override
    public Class<? extends Annotation> annotationType() {
      return this.getClass();
    }

    public int getId() {
      return this.id;
    }

    public TestCaseType getTestCaseType() {
      return this.type;
    }

    public List<Serializable> getLabels() {
      return Collections.unmodifiableList(this.labels);
    }

    public Tuple getTestCase() {
      return testCase;
    }

    public Factors getFactors() {
      return factors;
    }
  }

  public static enum TestCaseType {
    Custom,
    Generated,
    Violation
  }
}
