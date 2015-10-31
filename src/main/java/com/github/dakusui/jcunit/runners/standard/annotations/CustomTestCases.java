package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import org.junit.runners.model.FrameworkMethod;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Method;
import java.util.LinkedList;
import java.util.List;

/**
 * Annotation for static methods that returns an iterable of test cases ({@code Iterable<Tuple>}).
 * By using this, a user can explicitly define test cases.
 * <p/>
 * This annotation is useful to reproduce bugs which happen under known conditions.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target({ ElementType.METHOD })
@ValidateWith(CustomTestCases.Validator.class)
public @interface CustomTestCases {
  /**
   * A validator used for custom test case methods.
   */
  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      List<Exception> errors = new LinkedList<Exception>();
      Method mm = method.getMethod();
      if (!method.isPublic() && method.isStatic()
          && mm.getParameterTypes().length == 0 &&
          (Tuple.class.isAssignableFrom(mm.getReturnType()) ||
              (Iterable.class.isAssignableFrom(mm.getReturnType())
              ))) {
        errors.add(new Exception("error"));
      }
      return errors;
    }
  }
}
