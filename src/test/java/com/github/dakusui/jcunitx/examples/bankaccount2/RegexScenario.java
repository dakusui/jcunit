package com.github.dakusui.jcunitx.examples.bankaccount2;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

public interface RegexScenario {
  String regularExpression();

  @Retention(RUNTIME)
  @interface Handle {

  }
}
