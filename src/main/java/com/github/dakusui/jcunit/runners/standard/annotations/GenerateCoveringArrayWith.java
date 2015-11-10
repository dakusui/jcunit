package com.github.dakusui.jcunit.runners.standard.annotations;

import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.List;

@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@ValidateWith(GenerateCoveringArrayWith.Validator.class)
public @interface GenerateCoveringArrayWith {
  Generator engine() default @Generator();

  Checker checker() default @Checker();

  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedClass(TestClass testClass) {
      return super.validateAnnotatedClass(testClass);
    }

    @Override
    public List<Exception> validateAnnotatedField(FrameworkField field) {
      return super.validateAnnotatedField(field);
    }
  }
}

