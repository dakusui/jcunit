package com.github.jcunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface Given {
  String ALL_CONSTRAINTS = "*";

  /**
   * Returns an array of method names within the same class this annotation is
   * attached to.
   *
   * @return Names of all methods to that this annotation is attached.
   */
  String[] value() default { ALL_CONSTRAINTS };
}
