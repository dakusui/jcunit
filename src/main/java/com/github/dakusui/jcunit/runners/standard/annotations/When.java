package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.runners.standard.FrameworkMethodUtils;
import org.junit.Test;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.*;
import java.util.LinkedList;
import java.util.List;

/**
 * The {@code When} should be used with {@literal @}{@code Test} annotation of JUnit
 * and it tells JCUnit that the test method should only be invoked when the condition
 * defined by it is satisfied.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ReferrerAttribute("value")
@ValidateWith(When.Validator.class)
public @interface When {
  /**
   * Returns an array of method names within the same class this annotation is
   * attached to.
   * The condition the method referred by a value in the array, see {@link Condition}.
   *
   * @see ReferrerAttribute
   * @see Condition
   */
  String[] value();

  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      List<Exception> errors = new LinkedList<Exception>();
      if (method.getAnnotation(Test.class) == null) {
        errors.add(new Exception(Utils.format(
            "This annotation should be used with @%s annotation.", Test.class
        )));
      }

      validateMethodsReferenced(
          When.class,
          new TestClass(method.getDeclaringClass()),
          errors);
      return errors;
    }

    private void validateMethodsReferenced(Class<? extends Annotation> by, TestClass in, List<Exception> errors) {
      for (FrameworkMethod each : in.getAnnotatedMethods(by)) {
        String referrer = Checks.checknotnull(by.getAnnotation(ReferrerAttribute.class),
            "Probably framework issue. '%s' does not have %s annotation.",
            by,
            ReferrerAttribute.class
        ).value();
        String[] terms = Checks.cast(String[].class, ReflectionUtils.invoke(
            each.getAnnotation(by),
            ReflectionUtils.getMethod(by, referrer
            )));
        errors.addAll(validateReferencedMethods(in, referrer, terms));
      }
    }

    public static List<Exception> validateReferencedMethods(TestClass in, String referrerAttribute, String[] terms) {
      return new FrameworkMethodValidationHandler(
          in,
          referrerAttribute,
          new LinkedList<Exception>()
      ).handleTermArray(
          new ReferenceWalker<List<Exception>>(in, referrerAttribute),
          terms
      );
    }

    static class FrameworkMethodValidationHandler extends ReferenceHandler<List<Exception>> {
      private final List<Exception> errors;
      private final String          referrerName;
      private final TestClass       testClass;

      FrameworkMethodValidationHandler(TestClass testClass, String referrerName, List<Exception> errors) {
        this.testClass = testClass;
        this.referrerName = referrerName;
        this.errors = errors;
      }

      @Override
      public void handleMethod(ReferenceWalker<List<Exception>> walker, boolean negateOperator, FrameworkMethod method) {
        ////
        // Validation specific logic follows
        if (method instanceof FrameworkMethodUtils.NotFoundMethod) {
          errors.add(new Exception(
              Utils.format(
                  "Method '%s' referenced by '%s#%s' in '%s' was not found in the class. Check if the method is annotated with @%s.",
                  method.getName(),
                  When.class,
                  referrerName,
                  testClass.getJavaClass(),
                  Condition.class
              )));
        } else if (method.getAnnotation(Condition.class) == null) {
          errors.add(new Exception(
              Utils.format(
                  "Method '%s' referenced by '%s' in '%s' was found in the class but not annotated with @%s",
                  method.getName(),
                  referrerName,
                  testClass.getName(),
                  Condition.class
              )));
        }
      }

      @Override
      public void handleTerm(ReferenceWalker<List<Exception>> walker, String term) {
        walker.walk(this, term);
      }

      @Override
      public List<Exception> handleTermArray(ReferenceWalker<List<Exception>> walker, String[] terms) {
        walker.walk(this, terms);
        return this.errors;
      }
    }

  }
}

