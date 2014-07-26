package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraint.Constraint;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface SchemafulTupleGeneration {
  Generator generator() default @Generator();

  Constraint constraint() default @Constraint();
}
