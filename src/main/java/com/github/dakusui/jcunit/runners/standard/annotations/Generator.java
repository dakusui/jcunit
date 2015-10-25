package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.plugins.generators.IPO2TupleGenerator;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends TupleGenerator> value() default IPO2TupleGenerator.class;

  Value[] params() default { };
}
