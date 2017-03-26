package com.github.dakusui.jcunit8.runners.junit4.annotations;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.Parameter.Simple;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ParameterSource {
  Class<? extends Parameter> by() default Simple.class;
}
