package com.github.dakusui.jcunit8.examples.bankaccount2;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface RegexScenario {
  String regularExpression();

  @Retention(RUNTIME)
  @interface Handle {

  }
}
