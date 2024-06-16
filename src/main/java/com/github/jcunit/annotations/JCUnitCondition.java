package com.github.jcunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface JCUnitCondition {
  Type value() default Type.CONDITION;

  enum Type {
    CONSTRAINT,
    CONDITION
  }
}
