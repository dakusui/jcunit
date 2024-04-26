package com.github.jcunit.annotations;


import com.github.jcunit.core.model.FactorFactory;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface DefineParameter {
  String name();
  String[] with() default {};
  Class<? extends FactorFactory> as() default FactorFactory.StringLevels.class;
}
