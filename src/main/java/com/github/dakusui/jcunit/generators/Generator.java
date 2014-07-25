package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.Param;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends SchemafulTupleGenerator> value() default IPO2SchemafulTupleGenerator.class;

  Param[] params() default { };
}
