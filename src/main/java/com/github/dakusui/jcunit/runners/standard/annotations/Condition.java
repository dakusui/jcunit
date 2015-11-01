package com.github.dakusui.jcunit.runners.standard.annotations;

import org.junit.runners.model.FrameworkMethod;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ValidateWith(Condition.Validator.class)
public @interface Condition {
  class Validator extends AnnotationValidator {
    /**
     * Validates a precondition method.
     * A precondition method is a method annotated with {@literal @}{@code Precondition} or referred to by an annotation {@literal @}Given.
     * It is mainly used to determine if a test method (or methods) should be executed in advance.
     * <p/>
     * It must be public, non-static, returning a boolean value, and must have no parameter.
     * In case it is not valid, this method add a string message which describes the failure to {@code errors} list.
     */
    @Override
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      List<Exception> errors = new LinkedList<Exception>();
      Class testClass = method.getDeclaringClass();
      if (!method.isPublic()) {
        errors.add(new Exception(String.format(
            "The method '%s' must be public. (in %s)", method.getName(), testClass.getCanonicalName()
        )));
      }
      if (method.isStatic()) {
        errors.add(new Exception(String.format(
            "The method '%s' must not be static. (in %s)", method.getName(), testClass.getCanonicalName()
        )));
      }
      if (!Boolean.TYPE.equals(method.getReturnType())) {
        errors.add(new Exception(String.format(
            "The method '%s' must return a boolean value, but '%s' is returned. (in %s)",
            method.getName(),
            method.getReturnType().getName(),
            testClass.getCanonicalName()
        )));
      }
      Class<?>[] parameterTypes = method.getMethod().getParameterTypes();
      if (parameterTypes.length != 0) {
        errors.add(new Exception(String.format(
            "The method '%s' must not have any parameter. (in %s)",
            method.getName(),
            testClass.getCanonicalName()
        )));
      }
      return errors;
    }
  }
}
