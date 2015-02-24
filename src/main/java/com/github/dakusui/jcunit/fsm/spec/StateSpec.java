package com.github.dakusui.jcunit.fsm.spec;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
* Created by hiroshi on 1/15/15.
*/
@Retention(RetentionPolicy.RUNTIME)
public @interface StateSpec {
  boolean mandatory() default false;
}
