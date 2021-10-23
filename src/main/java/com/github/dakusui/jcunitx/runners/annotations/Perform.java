package com.github.dakusui.jcunitx.runners.annotations;

import java.lang.annotation.*;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Documented
@Inherited
@Retention(RUNTIME)
@Target(METHOD)
public @interface Perform {
  /**
   * A name of scenario to be performed by a method this annotation is attached to.
   */
  String value() default "";
}
