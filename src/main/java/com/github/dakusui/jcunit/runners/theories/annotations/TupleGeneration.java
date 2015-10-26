package com.github.dakusui.jcunit.runners.theories.annotations;

import com.github.dakusui.jcunit.runners.standard.annotations.Constraint;
import com.github.dakusui.jcunit.runners.standard.annotations.Generator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Not to be confused with another annotation of the same name in standard runner
 * package.
 */
@Target({ ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
public @interface TupleGeneration {
  Generator generator() default @Generator();

  Constraint constraint() default @Constraint();
}
