package com.github.jcunit.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(METHOD)
public @interface JCUnitParameterValue {
  /**
   * A tag to specify a method to which this annotation is attached.
   * The value must be unique inside a parameter space to which the annotated method belongs.
   *
   * @return A tag for the method to be annotated by this interface.
   */
  String value() default "";
}
