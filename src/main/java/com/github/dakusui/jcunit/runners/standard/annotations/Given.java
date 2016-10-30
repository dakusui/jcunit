package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
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
  String[] value() default {};

  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      List<Exception> errors = new LinkedList<Exception>();
      if (method.getAnnotation(Test.class) == null) {
        errors.add(new Exception(StringUtils.format(
            "This annotation should be used with @%s annotation.", Test.class
        )));
      }

      validateMethodsReferencedBy(
          When.class,
          new TestClass(method.getDeclaringClass()),
          errors);
      return errors;
    }

    private void validateMethodsReferencedBy(Class<? extends Annotation> by, TestClass in, List<Exception> errors) {
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
      return new ReferenceHandler.FrameworkMethodValidationHandler(
          in,
          referrerAttribute,
          new LinkedList<Exception>()
      ).handleTermArray(
          new ReferenceWalker<List<Exception>>(in, referrerAttribute),
          terms
      );
    }
  }
}

