package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.plugins.reporters.CoverageReporter;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Reporter {
  Class<? extends CoverageReporter> value() default CoverageReporter.Default.class;

  Value[] args() default { };
}
