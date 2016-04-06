package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.constraints.NullConstraintChecker;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Checker {
  class Default {
    public static final Checker INSTANCE = new Checker() {

      @Override
      public Class<? extends Annotation> annotationType() {
        return Checker.class;
      }

      @Override
      public Class<? extends ConstraintChecker> value() {
        return NullConstraintChecker.class;
      }

      @Override
      public Value[] args() {
        return new Value[] {};
      }
    };
  }

  Class<? extends ConstraintChecker> value() default NullConstraintChecker.class;

  Value[] args() default {};

}
