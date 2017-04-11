package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.CompatUtils;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.standard.annotations.*;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * A class that holds utility methods to retrieve and validate framework methods.
 */
public enum FrameworkMethodUtils {
  ;

  public static List<FrameworkMethod> getConditionMethods(TestClass testClass) {
    Checks.checknotnull(testClass);
    ConstraintChecker constraintChecker = getConstraintCheckerFrom(testClass);
    List<FrameworkMethod> ret = new LinkedList<FrameworkMethod>();
    ret.addAll(testClass.getAnnotatedMethods(Condition.class));
    final JCUnitFrameworkMethod.FromConstraintChecker builder
        = new JCUnitFrameworkMethod.FromConstraintChecker(constraintChecker);
    ret.addAll(Utils.transform(
        Utils.concatenate(constraintChecker.getTags(), "*"),
        new Utils.Form<String, FrameworkMethod>() {
          @Override
          public FrameworkMethod apply(String in) {
            return builder.buildWith(in);
          }
        }));
    return ret;
  }

  public static CompositeFrameworkMethod buildCompositeFrameworkMethod(TestClass testClass, Annotation ann) {
    Checks.checknotnull(testClass);
    Checks.checknotnull(ann);

    ReferrerAttribute referrerAttribute = Checks.checknotnull(
        ann.annotationType().getAnnotation(ReferrerAttribute.class),
        "Annotation '%s' doesn't have %s.", ann, ReferrerAttribute.class);

    ReferenceWalker<CompositeFrameworkMethod> referenceWalker = new ReferenceWalker<CompositeFrameworkMethod>(
        testClass,
        referrerAttribute.value()
    );
    return buildCompositeFrameworkMethod(referenceWalker, ann, referrerAttribute);
  }

  public static CompositeFrameworkMethod buildCompositeFrameworkMethod(ReferenceWalker<CompositeFrameworkMethod> referenceWalker, Annotation ann, ReferrerAttribute referrerAttribute) {
    return new ReferenceHandler.Base(CompositeFrameworkMethod.Mode.Or).handleTermArray(
        referenceWalker,
        CompatUtils.cast(
            String[].class,
            ReflectionUtils.invokeForcibly(
                ann,
                ReflectionUtils.getMethod(ann.getClass(), "value")
            )
        )
    );
  }

  private static ConstraintChecker getConstraintCheckerFrom(TestClass testClass) {
    GenerateCoveringArrayWith ann = testClass.getAnnotation(GenerateCoveringArrayWith.class);
    if (ann == null) {
      return ConstraintChecker.DEFAULT_CONSTRAINT_CHECKER;
    }
    return new ConstraintChecker.Builder(
        ann.checker(),
        testClass.getJavaClass()
    ).build();
  }

  /**
   * A model class for JCUnit specific FrameworkMethods.
   */
  public static abstract class JCUnitFrameworkMethod extends FrameworkMethod {
    public static final Method DUMMY_METHOD;

    static {
      try {
        DUMMY_METHOD = CompositeFrameworkMethod.class.getMethod("dummyMethod");
      } catch (NoSuchMethodException e) {
        throw Checks.wrap(e);
      }
    }

    /**
     * Creates a new {@code FrameworkMethod} for {@code method}
     */
    public JCUnitFrameworkMethod(Method method) {
      super(method);
    }

    public JCUnitFrameworkMethod() {
      this(DUMMY_METHOD);
    }

    @Override
    public abstract Object invokeExplosively(final Object target, final Object... params) throws Throwable;

    @Override
    public abstract String getName();

    /**
     * A fluent builder constructor which creates a {@code FrameworkMethod} object
     * from a constraint checker.
     */
    public static class FromConstraintChecker {
      private final ConstraintChecker constraintChecker;

      public FromConstraintChecker(ConstraintChecker constraintChecker) {
        this.constraintChecker = Checks.checknotnull(constraintChecker);
      }

      public FrameworkMethod buildWith(final String name) {
        if ("*".equals(name)) {
          return new JCUnitFrameworkMethod() {
            @Override
            public Object invokeExplosively(Object target, Object... params) throws Throwable {
              for (String tag : FromConstraintChecker.this.constraintChecker.getTags()) {
                if ("*".equals(tag)) continue;
                if (FromConstraintChecker.this.constraintChecker.violates(TestCaseUtils.toTestCase(target), tag)) {
                  return false;
                }
              }
              return true;
            }

            @Override
            public String getName() {
              return "#*";
            }
          };
        }
        return new JCUnitFrameworkMethod() {
          @Override
          public Object invokeExplosively(Object target, Object... params) throws Throwable {
            return !FromConstraintChecker.this.constraintChecker.violates(
                TestCaseUtils.toTestCase(target),
                name
            );
          }

          @Override
          public String getName() {
            return String.format("#%s", name);
          }
        };
      }
    }
  }

  /**
   * Represents a JCUnit framework method which returns negated value of the original method.
   */
  static class NegatedFrameworkMethod extends JCUnitFrameworkMethod {
    private final FrameworkMethod enclosedMethod;

    /**
     * Returns a new {@code FrameworkMethod} for {@code method}
     */
    public NegatedFrameworkMethod(FrameworkMethod method) {
      super(method.getMethod()); // Use original method.
      this.enclosedMethod = method;
    }

    @Override
    public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
      return !((Boolean) this.enclosedMethod.invokeExplosively(target, params));
    }

    @Override
    public String getName() {
      return String.format("!%s", this.enclosedMethod.getName());
    }
  }

  public static String composePreconditionCompositeFrameworkMethodName(Given ann) {
    Checks.checknotnull(ann);
    StringBuilder b = new StringBuilder();
    String[] values = ann.value();
    if (values.length > 1) {
      b.append('(');
    }
    boolean firstTime = true;
    for (String each : values) {
      if (!firstTime) {
        b.append("||");
      }
      b.append(normalize(each));
      firstTime = false;
    }
    if (values.length > 1) {
      b.append(')');
    }
    return b.toString();
  }

  private static String normalize(String term) {
    StringBuilder b = new StringBuilder();
    String[] tokens = term.replace(" ", "").split("&&");
    if (tokens.length > 1)
      b.append('(');
    boolean firstTime = true;
    for (String each : tokens) {
      if (!firstTime)
        b.append("&&");
      b.append(each);
      firstTime = false;
    }
    if (tokens.length > 1)
      b.append(')');
    return b.toString();
  }
}
