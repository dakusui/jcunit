package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.plugins.constraintmanagers.ConstraintManager;
import com.github.dakusui.jcunit.plugins.constraintmanagers.NullConstraintManager;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Constraint {
  Class<? extends ConstraintManager> value() default NullConstraintManager.class;

  Value[] params() default { };
}
