package com.github.dakusui.jcunit8.runners.junit4.annotations;

import org.junit.experimental.theories.ParametersSuppliedBy;
import org.junit.experimental.theories.internal.SpecificDataPointsSupplier;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(PARAMETER)
@ParametersSuppliedBy(SpecificDataPointsSupplier.class)
@ValidateWith(From.Validator.class)
public @interface From {
  String value();

  class Validator extends AnnotationValidator {

  }
}
