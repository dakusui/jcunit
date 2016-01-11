package com.github.dakusui.jcunit.fsm.spec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ActionSpec {
  String DEFAULT_PARAMS_SPEC = "";
  String DEFAULT_ALIAS       = "";

  String parametersSpec() default DEFAULT_PARAMS_SPEC;

  /**
   * If {@code name()} is not specified for a method to which this annotation is attached,
   * JCUnit considers that the method's name is specified.
   *
   * If you explicitly specify the value of {@code DEFAULT_ALIAS}, it will treat as if
   * you didn't specify it.
   */
  String alias() default DEFAULT_ALIAS;

}
