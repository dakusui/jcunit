package com.github.dakusui.jcunitx.engine.junit5.compat;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ArgumentsSources {
  ArgumentsSource[] value();
}
