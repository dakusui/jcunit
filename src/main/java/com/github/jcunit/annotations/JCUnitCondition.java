package com.github.jcunit.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface JCUnitCondition {
  Type value() default Type.CONDITION;

  enum Type {
    CONSTRAINT,
    CONDITION
  }
}
