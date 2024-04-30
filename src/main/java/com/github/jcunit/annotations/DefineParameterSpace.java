package com.github.jcunit.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface DefineParameterSpace {
  DefineParameter[] parameters() default {};
  DefineConstraint[] constraints() default {};
}
