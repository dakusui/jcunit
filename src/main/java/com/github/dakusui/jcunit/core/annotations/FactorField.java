package com.github.dakusui.jcunit.core.annotations;

import com.github.dakusui.jcunit.core.factorfactories.DefaultFactorFactory;
import com.github.dakusui.jcunit.core.factorfactories.FactorFactory;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactorField {
  public Class<? extends FactorFactory> factory() default DefaultFactorFactory.class;
}
