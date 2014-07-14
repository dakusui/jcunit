package com.github.dakusui.jcunit.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestCaseGeneration {
  Generator generator() default @Generator();

  Constraint constraint() default @Constraint();
}
