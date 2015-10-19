package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.DefaultLevelsProvider;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;

import java.lang.annotation.*;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;

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
      "",
  };

  Class<? extends Enum> enumLevels() default Enum.class;

  /**
   * If {@code stringLevels} or {@code enumLevels} are being used, determines
   * if a {@code null} value is included in the levels.
   */
  boolean includeNull() default false;

  Class<? extends LevelsProvider> levelsProvider() default DefaultLevelsProvider.class;

  Param[] providerParams() default {};

  class Utils {
    public static Utils INSTANCE = new Utils();
    private final Map<Class<?>, Method> methodNameMappings;

    private Utils() {
      Map<Class<?>, Method> methodNameMappings = new HashMap<Class<?>, Method>();

      methodNameMappings.put(Boolean.TYPE, getLevelsMethod("booleanLevels"));
      methodNameMappings.put(Boolean.class, getLevelsMethod("booleanLevels"));
      methodNameMappings.put(Byte.TYPE, getLevelsMethod("byteLevels"));
      methodNameMappings.put(Byte.class, getLevelsMethod("byteLevels"));
      methodNameMappings.put(Character.TYPE, getLevelsMethod("charLevels"));
      methodNameMappings.put(Character.class, getLevelsMethod("charLevels"));
      methodNameMappings.put(Short.TYPE, getLevelsMethod("shortLevels"));
      methodNameMappings.put(Short.class, getLevelsMethod("shortLevels"));
      methodNameMappings.put(Integer.TYPE, getLevelsMethod("intLevels"));
      methodNameMappings.put(Integer.class, getLevelsMethod("intLevels"));
      methodNameMappings.put(Long.TYPE, getLevelsMethod("longLevels"));
      methodNameMappings.put(Long.class, getLevelsMethod("longLevels"));
      methodNameMappings.put(Float.TYPE, getLevelsMethod("floatLevels"));
      methodNameMappings.put(Float.class, getLevelsMethod("floatLevels"));
      methodNameMappings.put(Double.TYPE, getLevelsMethod("doubleLevels"));
      methodNameMappings.put(Double.class, getLevelsMethod("doubleLevels"));
      methodNameMappings.put(String.class, getLevelsMethod("stringLevels"));
      methodNameMappings.put(Enum.class, getLevelsMethod("enumLevels"));

      this.methodNameMappings = methodNameMappings;
    }

    private Method getLevelsMethod(String methodName) {
      return ReflectionUtils.getMethod(FactorField.class, methodName);
    }

    public boolean hasMethodFor(Class<?> type) {
      return this.methodNameMappings.containsKey(Checks.checknotnull(type));
    }

    public Method getMethodFor(Class<?> type) {
      return this.methodNameMappings.get(Checks.checknotnull(type));
    }
  }

  class DefaultValues {
    public static final DefaultValues INSTANCE = new DefaultValues();

    @FactorField
    public final Object defaultValues = null;

    private DefaultValues() {
    }

    public boolean[] booleanLevels() {
      return get().booleanLevels();
    }

    public byte[] byteLevels() {
      return get().byteLevels();
    }

    public char[] charLevels() {
      return get().charLevels();
    }

    public short[] shortLevels() {
      return get().shortLevels();
    }

    public int[] intLevels() {
      return get().intLevels();
    }

    public long[] longLevels() {
      return get().longLevels();
    }

    public float[] floatLevels() {
      return get().floatLevels();
    }

    public double[] doubleLevels() {
      return get().doubleLevels();
    }

    public String[] stringLevels() {
      return get().stringLevels();
    }

    public Class<? extends Enum> enumLevels() {
      return get().enumLevels();
    }

    private static FactorField get() {
      return ReflectionUtils.getField(DefaultValues.class, "defaultValues").getAnnotation(FactorField.class);
    }
  }
}
