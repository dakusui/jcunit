package com.github.dakusui.jcunit.runners.standard.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for static methods that returns an iterable of test cases ({@code Iterable<Tuple>}).
 * By using this, a user can explicitly define test cases.
 *
 * This annotation is useful to reproduce bugs which happen under known conditions.
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTestCases {
}
