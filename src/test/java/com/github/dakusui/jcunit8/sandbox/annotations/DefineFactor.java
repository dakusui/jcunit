package com.github.dakusui.jcunit8.sandbox.annotations;


import com.github.dakusui.jcunit8.sandbox.core.FactorFactory;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface DefineFactor {
  String name();
  Class<? extends FactorFactory> value() default FactorFactory.class;
  String[] args() default {};
}
