package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.plugins.caengines.IPO2CAEngine;
import com.github.dakusui.jcunit.plugins.caengines.CAEngine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends CAEngine> value() default IPO2CAEngine.class;

  Value[] args() default { };
}
