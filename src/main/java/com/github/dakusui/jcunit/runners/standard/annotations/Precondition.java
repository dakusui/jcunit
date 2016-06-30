package com.github.dakusui.jcunit.runners.standard.annotations;

import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation for a filter method.
 */
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ValidateWith(Condition.Validator.class)
@ReferrerAttribute("values")
public @interface Precondition {
}
