package com.github.dakusui.jcunit8.sandbox.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(TYPE)
public @interface DefineFactorSpace {
  DefineFactor[] factors() default {};

}
