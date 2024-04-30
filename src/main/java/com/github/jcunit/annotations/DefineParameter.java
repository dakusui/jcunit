package com.github.jcunit.annotations;


import com.github.jcunit.core.model.ParameterFactory;

import java.lang.annotation.Retention;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
public @interface DefineParameter {
  String name();
  String[] with() default {};
  Class<? extends ParameterFactory> as() default ParameterFactory.StringLevels.class;
}
