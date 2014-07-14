package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.constraints.ConstraintManager;
import com.github.dakusui.jcunit.constraints.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;

import javax.jws.soap.SOAPBinding;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {
  Class<? extends TestCaseGenerator> value() default IPO2TestCaseGenerator.class;

  Param[] parameters() default { };

  Class<? extends ConstraintManager> constraintManager() default NullConstraintManager.class;
}
