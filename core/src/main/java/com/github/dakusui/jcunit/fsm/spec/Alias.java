package com.github.dakusui.jcunit.fsm.spec;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * By using this annotation, you can define an alias for a parameter.
 * This alias will be used by JCUnit when you are handling input histories.
 *
 */
@Retention(value = RetentionPolicy.RUNTIME)
@Target({ ElementType.PARAMETER})
public @interface Alias {
  String value();
}
