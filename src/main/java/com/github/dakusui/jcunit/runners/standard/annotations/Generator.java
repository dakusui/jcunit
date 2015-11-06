package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.plugins.caengines.IPO2CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends CoveringArrayEngine> value() default IPO2CoveringArrayEngine.class;

  Value[] args() default { };
}
