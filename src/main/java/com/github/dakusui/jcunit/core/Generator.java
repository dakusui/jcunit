package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.generators.TupleGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends TupleGenerator> value() default IPO2TupleGenerator.class;

  Param[] params() default { };
}
