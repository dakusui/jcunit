package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.constraint.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.Param;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
  Class<? extends ConstraintManager> value() default NullConstraintManager.class;

  Param[] params() default { };
}
