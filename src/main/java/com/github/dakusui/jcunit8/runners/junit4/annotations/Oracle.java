package com.github.dakusui.jcunit8.runners.junit4.annotations;

import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@ValidateWith(Oracle.Validator.class)
public @interface Oracle {
  class Validator extends AnnotationValidator {
  }
}
