package com.github.dakusui.jcunit.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface GeneratorParameters {
  public static enum Type {
    Int, IntArray, Long, LongArray, String, StringArray, Class, ClassArray
  }

  Value[] value();

  @Retention(RetentionPolicy.RUNTIME)
  public static @interface Value {

    String name() default "";

    Type type();

    int intValue() default 0;

    int[] intArrayValue() default {};

    long longValue() default 0;

    long[] longArrayValue() default {};

    Class<? extends Object> classValue() default Object.class;

    Class<? extends Object>[] classArrayValur() default {};

    String stringValue() default "";

    String[] stringArrayValue() default {};
  }
}
