package com.github.dakusui.jcunit8.runners.junit4.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;


@Retention(RUNTIME)
@Target(PARAMETER)
public @interface From {
  String value();
}
