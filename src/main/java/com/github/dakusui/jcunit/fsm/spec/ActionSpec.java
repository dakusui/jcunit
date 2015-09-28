package com.github.dakusui.jcunit.fsm.spec;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionSpec {
  Class<? extends FSMSpec> child() default FSMSpec.class;
}
