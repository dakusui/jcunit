package com.github.jcunit.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface DefineConstraint {
  /**
   * Specifies a name of a constraint defined by this entry.
   * If you don't give a name to a constraint, it will be considered "unnamed".
   * When you want to generate negative tests, you need to specify a name of a constraint which the negative tests should violate.
   *
   * @return A name of this constraint.
   */
  String name() default "";
  String[] value() default {};
}
