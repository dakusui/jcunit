package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.generators.TestCaseGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends TestCaseGenerator> value() default IPO2TestCaseGenerator.class;

  String[] parameters() default { };
}
