package com.github.dakusui.jcunit.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface TestExecution {
  /**
   * If '-1' is the only element of this attribute, all the test cases will
   * be executed.
   */
  public int[] include() default {-1};

  public int[] exclude() default {};
}
