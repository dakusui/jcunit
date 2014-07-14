package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.constraints.constraintmanagers.NullConstraintManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
  Class<? extends ConstraintManager> value() default NullConstraintManager.class;

  Param[] params() default { };
}
