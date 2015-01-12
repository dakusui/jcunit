package com.github.dakusui.jcunit.fsm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface FSMSpec {
  /**
   * Methods that represent state transition functions are annotated with this.
   * A method annotated with this must return a state object and the first parameter
   * of it must always be an object of {@code SUT}.
   * <p/>
   * Also, the first parameter must not be annotated with {@code @Parameter} since
   * it is not a parameter.
   */
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Transition {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Initial {
  }
}
