package com.github.dakusui.jcunit.compat.core.annotations;

import com.github.dakusui.jcunit.compat.generators.TestArrayGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {

  @SuppressWarnings("rawtypes") Class<? extends TestArrayGenerator> value();

}
