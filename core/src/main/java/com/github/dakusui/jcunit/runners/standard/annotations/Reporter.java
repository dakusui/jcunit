package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.coverage.Metrics;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Reporter {
  Class<? extends Metrics> value() default Metrics.class;

  Value[] args() default { };
}
