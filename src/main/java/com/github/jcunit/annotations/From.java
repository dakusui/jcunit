package com.github.jcunit.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import static java.lang.annotation.ElementType.PARAMETER;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(PARAMETER)
public @interface From {
  int ALL = -1;

  String value() default "";
  int index() default 0;
}
