package com.github.jcunit.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface JCUnitParameterValue {
  /**
   * A tag to specify a method to which this annotation is attached.
   * The value must be unique inside a parameter space to which the annotated method belongs.
   *
   * @return A tag for the method to be annotated by this interface.
   */
  String value() default "";
}
