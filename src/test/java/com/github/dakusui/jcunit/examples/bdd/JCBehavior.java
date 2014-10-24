package com.github.dakusui.jcunit.examples.bdd;

import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.JCUnitRunner;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.generators.TupleGeneratorFactory;
import org.junit.internal.runners.model.ReflectiveCallable;
import org.junit.internal.runners.statements.Fail;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.InitializationError;
import org.junit.runners.model.Statement;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;

public class JCBehavior extends JCUnit {
  private static final String FACTOR_NAME_GIVEN_METHOD = "$given_method";

  /**
   * Only called reflectively by JUnit. Do not use programmatically.
   *
   * @param klass
   */
  public JCBehavior(Class<?> klass) throws Throwable {
    super(klass);
  }

  @Override
  protected TupleGeneratorFactory getTupleGeneratorFactory() {
    return new TupleGeneratorFactory() {
      @Override
      protected Factors loadFactors(Class<?> klass) {
        Factors.Builder b = new Factors.Builder();
        for (Factor each : super.loadFactors(klass)) {
          if (FACTOR_NAME_GIVEN_METHOD.equals(each.name)) {
            throw new InvalidTestException(
                String.format(
                    "You cannot define a factor '%s' in '%s' to use JCBehavior functionality.",
                    FACTOR_NAME_GIVEN_METHOD,
                    klass.getCanonicalName()
                ),
                null);
          }
          b.add(each);
        }
        Factor.Builder bb = new Factor.Builder();
        bb.setName(FACTOR_NAME_GIVEN_METHOD);
        for (FrameworkMethod each : getMethodsReferencedByGiven(klass)) {
          bb.addLevel(each);
        }
        b.add(bb.build());
        return b.build();
      }
    };
  }

  @Override
  protected JCUnitRunner createRunner(int id, Factors factors, TestCaseType testCaseType, Tuple testCase) throws InitializationError {
    return new JCUnitRunner(getTestClass().getJavaClass(), id, testCaseType, factors, testCase) {
      @Override
      protected Statement methodBlock(FrameworkMethod method) {
        Object test;
        try {
          test = new ReflectiveCallable() {
            @Override
            protected Object runReflectiveCall() throws Throwable {
              return createTest();
            }
          }.run();
        } catch (Throwable e) {
          return new Fail(e);
        }
        return withThens(method, test, withGivens(method, test, super.methodBlock(method)));
      }

      private Statement withGivens(FrameworkMethod method, Object test, Statement statement) {
        return statement;
      }

      private Statement withThens(FrameworkMethod method, Object test, Statement statement) {
        return statement;
      }

    };
  }

  protected List<FrameworkMethod> getMethodsReferencedByGiven(Class<?> klass) {
    List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
    return ret;
  }

  protected List<FrameworkMethod> getMethodsReferencedByThen(Class<?> klass) {
    List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
    return ret;
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Given {
    Param[] value() default {};
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface When {
    Param[] value() default {};
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.METHOD)
  public static @interface Then {
    Param[] value() default {};
  }
}
