package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.apiguardian.api.API;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ ElementType.ANNOTATION_TYPE, ElementType.METHOD })
@Retention(RetentionPolicy.RUNTIME)
@ArgumentsSource(ValueArgumentsProvider.class)
@API(status = API.Status.STABLE)
public @interface ValueSource {

  /**
   * The {@code short} values to use as sources of arguments; must not be empty.
   *
   * @since 5.1
   */
  short[] shorts() default {};

  /**
   * The {@code byte} values to use as sources of arguments; must not be empty.
   *
   * @since 5.1
   */
  byte[] bytes() default {};

  /**
   * The {@code int} values to use as sources of arguments; must not be empty.
   */
  int[] ints() default {};

  /**
   * The {@code long} values to use as sources of arguments; must not be empty.
   */
  long[] longs() default {};

  /**
   * The {@code float} values to use as sources of arguments; must not be empty.
   *
   * @since 5.1
   */
  float[] floats() default {};

  /**
   * The {@code double} values to use as sources of arguments; must not be empty.
   */
  double[] doubles() default {};

  /**
   * The {@code char} values to use as sources of arguments; must not be empty.
   *
   * @since 5.1
   */
  char[] chars() default {};

  /**
   * The {@code boolean} values to use as sources of arguments; must not be empty.
   *
   * @since 5.5
   */
  boolean[] booleans() default {};

  /**
   * The {@link String} values to use as sources of arguments; must not be empty.
   */
  String[] strings() default {};

  /**
   * The {@link Class} values to use as sources of arguments; must not be empty.
   *
   * @since 5.1
   */
  Class<?>[] classes() default {};
}
