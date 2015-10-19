package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

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
    public final Object defaultValues = null;

    private DefaultValues() {}

    public static boolean[] booleanLevels() {
      return get().booleanLevels();
    }

    public static byte[] byteLevels() {
      return get().byteLevels();
    }

    public static char[] charLevels() {
      return get().charLevels();
    }

    public static short[] shortLevels() {
      return get().shortLevels();
    }

    public static int[] intLevels() {
      return get().intLevels();
    }

    public static  long[] longLevels() {
      return get().longLevels();
    }

    public static float[] floatLevels() {
      return get().floatLevels();
    }

    public static double[] doubleLevels() {
      return get().doubleLevels();
    }

    public static String[] stringLevels() {
      return get().stringLevels();
    }

    public static Class<? extends Enum> enumLevels() {
      return get().enumLevels();
    }

    private static FactorField get() {
      return ReflectionUtils.getField(DefaultValues.class, "defaultValues").getAnnotation(FactorField.class);
    }
  }
}
