package com.github.jcunit.runners.junit4.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
public @interface Condition {

  /**
   * Returns if this condition is a constraint or not
   *
   * @return tells if this condition is a constraint or not.
   */
  boolean constraint() default false;
}
