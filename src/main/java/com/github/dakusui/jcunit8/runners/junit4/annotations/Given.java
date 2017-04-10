package com.github.dakusui.jcunit8.runners.junit4.annotations;

import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@ValidateWith(Given.Validator.class)
public @interface Given {
  String ALL_CONSTRAINTS = "*";

  /**
   * Returns an array of method names within the same class this annotation is
   * attached to.
   */
  String[] value() default { ALL_CONSTRAINTS };

  class Validator extends AnnotationValidator {
  }
}
