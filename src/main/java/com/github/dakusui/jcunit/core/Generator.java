package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.generators.IPO2SchemafulTupleGenerator;
import com.github.dakusui.jcunit.generators.SchemafulTupleGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends SchemafulTupleGenerator> value() default IPO2SchemafulTupleGenerator.class;

  Param[] params() default { };
}
