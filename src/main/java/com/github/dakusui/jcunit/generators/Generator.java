package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.generators.IPO2TestCaseGenerator;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends TestCaseGenerator> value() default IPO2TestCaseGenerator.class;

  Param[] params() default { };
}
