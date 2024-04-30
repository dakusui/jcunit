package com.github.jcunit.annotations;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
@Retention(RUNTIME)
public @interface UsingParameterSpace {
  Class<?> value() default Object.class;
}
