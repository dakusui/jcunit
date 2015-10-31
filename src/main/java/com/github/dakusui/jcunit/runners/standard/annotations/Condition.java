package com.github.dakusui.jcunit.runners.standard.annotations;

import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ValidateWith(Precondition.Validator.class)
public @interface Condition {
}
