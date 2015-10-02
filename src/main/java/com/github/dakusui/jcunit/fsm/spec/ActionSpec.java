package com.github.dakusui.jcunit.fsm.spec;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionSpec {
  String DEFAULT_PARAMS_SPEC = "";

  String parametersSpec() default DEFAULT_PARAMS_SPEC;
}
