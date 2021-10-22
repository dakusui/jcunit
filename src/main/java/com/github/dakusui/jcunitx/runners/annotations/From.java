package com.github.dakusui.jcunitx.runners.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Target(PARAMETER)
@Retention(RUNTIME)
@Documented
@Inherited
public @interface From {
  /**
   * A name of a testing parameter from which the parameter value is retrieved.
   */
  String value();
}
