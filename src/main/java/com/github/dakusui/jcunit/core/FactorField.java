package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.InvocationTargetException;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface FactorField {
  boolean[] booleanLevels() default { true, false };

  byte[] byteLevels() default { (byte) 1, (byte) 0, (byte) -1, (byte) 100,
      (byte) -100, Byte.MAX_VALUE, Byte.MIN_VALUE };

  char[] charLevels() default { 'a', 'あ', (char) 1, Character.MAX_VALUE,
      Character.MIN_VALUE };

  short[] shortLevels() default { (short) 1, (short) 0, (short) -1,
      (short) 100, (short) -100, Short.MAX_VALUE, Short.MIN_VALUE };

  int[] intLevels() default { 1, 0, -1, 100, -100, Integer.MAX_VALUE,
      Integer.MIN_VALUE };

  long[] longLevels() default { 1L, 0L, -1L, 100L, -100L, Long.MAX_VALUE,
      Long.MIN_VALUE };

  float[] floatLevels() default { 1.1f, 0f, -1.1f, 100.0f, -100.0f,
      Float.MAX_VALUE, Float.MIN_VALUE };

  double[] doubleLevels() default { 1.0d, 0d, -1.0d, 100.0d, -100.0d,
      Double.MAX_VALUE, Double.MIN_VALUE };

  String[] stringLevels() default {
      "Hello world", "こんにちは世界", "1234567890", "ABCDEFGHIJKLMKNOPQRSTUVWXYZ",
      "abcdefghijklmnopqrstuvwxyz", "`-=~!@#$%^&*()_+[]\\{}|;':\",./<>?", " ",
      ""
  };

  Class<? extends Enum> enumLevels() default Enum.class;

  /**
   * If {@code stringLevels} or {@code enumLevels} are being used, determines
   * if a {@code null} value is included in the levels.
   */
  boolean includeNull() default false;

  Class<? extends LevelsProvider> levelsProvider() default DefaultLevelsProvider.class;

  Param[] providerParams() default {};

  class DefaultValues {
    @FactorField
    private Object defaultValues;

    boolean[] booleanLevels() {
      return get().booleanLevels();
    }

    byte[] byteLevels() {
      return get().byteLevels();
    }

    char[] charLevels() {
      return get().charLevels();
    }

    short[] shortLevels() {
      return get().shortLevels();
    }

    int[] intLevels() {
      return get().intLevels();
    }

    long[] longLevels() {
      return get().longLevels();
    }

    float[] floatLevels() {
      return get().floatLevels();
    }

    double[] doubleLevels() {
      return get().doubleLevels();
    }

    String[] stringLevels() {
      return get().stringLevels();
    }

    <T extends Enum> T[] enumLevels(Class<T> enumClass) {
      try {
        return (T[]) Checks.checknotnull(enumClass).getMethod("values").invoke(null);
      } catch (IllegalAccessException e) {
        ///
        // This path will never be executed.
        Checks.rethrow(e);
      } catch (InvocationTargetException e) {
        ///
        // This path will never be executed.
        Checks.rethrow(e);
      } catch (NoSuchMethodException e) {
        ///
        // This path will never be executed.
        Checks.rethrow(e);
      }
      ///
      // This path will never be executed.
      return null;
    }

    private static FactorField get() {
      try {
        return DefaultValues.class.getField("defaultValues").getAnnotation(FactorField.class);
      } catch (NoSuchFieldException e) {
        ///
        // This path will never be executed.
        Checks.rethrow(e);
      }
      ///
      // This path will never be executed.
      return null;
    }
  }
}
