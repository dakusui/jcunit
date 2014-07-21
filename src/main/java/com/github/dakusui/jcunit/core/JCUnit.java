package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.Violation;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;
import com.github.dakusui.jcunit.generators.TestCaseGeneratorFactory;
import org.junit.runner.Runner;
import org.junit.runners.Suite;
import org.junit.runners.model.FrameworkMethod;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class JCUnit extends Suite {
  private final ArrayList<Runner> runners = new ArrayList<Runner>();

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   */
  public JCUnit(Class<?> klass) throws Throwable {
    super(klass, Collections.<Runner>emptyList());
    TestCaseGenerator testCaseGenerator = TestCaseGeneratorFactory.INSTANCE
        .createTestCaseGenerator(klass);
    int id;
    for (id = 0; id < testCaseGenerator.size(); id++) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          JCUnitTestCaseType.Normal, id, testCaseGenerator.get(id)));
    }
    ConstraintManager cm = testCaseGenerator.getConstraintManager();
    final List<Violation> violations = cm.getViolations();
    for (Violation violation : violations) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          JCUnitTestCaseType.Violation, violation.getId(),
          violation.getTestCase()));
      id++;
    }
    for (Tuple tuple : allCustomTuples()) {
      runners.add(new JCUnitRunner(getTestClass().getJavaClass(),
          JCUnitTestCaseType.Custom, id, tuple));
      id++;
    }
  }

  @Override
  protected List<Runner> getChildren() {
    return runners;
  }

  @SuppressWarnings("unchecked")
  private Iterable<Tuple> allCustomTuples() throws Throwable {
    Object parameters = getParametersMethod().invokeExplosively(null);
    if (parameters instanceof Iterable) {
      return (Iterable<Tuple>) parameters;
    } else {
      throw parametersMethodReturnedWrongType();
    }
  }

  private FrameworkMethod getParametersMethod() throws Exception {
    List<FrameworkMethod> methods = getTestClass().getAnnotatedMethods(
        Parameters.class);
    for (FrameworkMethod each : methods) {
      if (each.isStatic() && each.isPublic()) {
        return each;
      }
    }
    throw new Exception("No public static parameters method on class "
        + getTestClass().getName());
  }

  private Exception parametersMethodReturnedWrongType() throws Exception {
    String className = getTestClass().getName();
    String methodName = getParametersMethod().getName();
    String message = MessageFormat.format(
        "{0}.{1}() must return an Iterable of arrays.",
        className, methodName);
    return new Exception(message);
  }

  @Target(ElementType.METHOD)
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Parameters {
  }
}
