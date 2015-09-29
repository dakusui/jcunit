package com.github.dakusui.jcunit.fsm.spec;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface ActionSpec {
<<<<<<< HEAD
=======
  Class<? extends FSMSpec> child() default FSMSpec.class;
>>>>>>> cb89a0b7e20328958a04b4e8dfc8664278ca90c1
}
