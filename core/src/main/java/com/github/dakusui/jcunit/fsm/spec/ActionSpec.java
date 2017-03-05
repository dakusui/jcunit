package com.github.dakusui.jcunit.fsm.spec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target({ElementType.METHOD})
public @interface ActionSpec {
  String DEFAULT_PARAMS_SPEC = "";

  String parametersSpec() default DEFAULT_PARAMS_SPEC;
}
