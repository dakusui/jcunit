package com.github.dakusui.jcunit.standardrunner.annotations;

import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.plugins.constraintmanagers.NullConstraintManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
  Class<? extends ConstraintManager> value() default NullConstraintManager.class;

  Param[] params() default { };
}
