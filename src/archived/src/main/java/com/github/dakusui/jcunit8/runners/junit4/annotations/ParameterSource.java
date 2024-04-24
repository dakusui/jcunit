package com.github.dakusui.jcunit8.runners.junit4.annotations;

import org.junit.runners.model.FrameworkMethod;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.LinkedList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@ValidateWith(ParameterSource.Validator.class)
public @interface ParameterSource {
  class Validator extends AnnotationValidator {
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      return new LinkedList<Exception>() {{
        if (!method.isPublic())
          add(new Exception(
              String.format("'%s' must be public but not.", method.getName())
          ));
        if (method.getMethod().getParameterCount() > 0) {
          add(new Exception(
              String.format("'%s' must not have any parameter but has one or more.", method.getName())
          ));
        }
        if (method.isStatic()) {
          add(new Exception(
              String.format("'%s' must not be static but is.", method.getName())
          ));
        }
      }};
    }
  }
}
