package com.github.dakusui.jcunit.fsm;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

public interface FSMSpec<SUT> extends StateChecker<SUT> {
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface State {
  }
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Action {
  }
  @Retention(RetentionPolicy.RUNTIME)
  public static @interface ActionParameters {
  }
}
