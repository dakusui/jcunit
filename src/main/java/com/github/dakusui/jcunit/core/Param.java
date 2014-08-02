package com.github.dakusui.jcunit.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.Arrays;

@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
  String[] value();

}
