package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.plugins.Plugin;
import com.github.dakusui.jcunit.plugins.caengines.CAEngine;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import org.junit.runners.model.FrameworkField;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.AbstractList;
import java.util.LinkedList;
import java.util.List;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ValidateWith(FactorField.Validator.class)
public @interface FactorField {
  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedField(FrameworkField field) {
      // TODO
      return super.validateAnnotatedField(field);
    }
  }

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
      "abcdefghijklmnopqrstuvwxyz", "`-=~!@#$%^&*()_+[]\\{}|;':\",./<>?", " "
  };

  Class<? extends LevelsProvider> levelsProvider() default FactorFactory.Default.DummyLevelsProvider.class;

  /**
   * If {@code stringLevels} or {@code enumLevels} are being used, determines
   * if a {@code null} value is included in the levels.
   */
  boolean includeNull() default false;

  Value[] providerParams() default {};

  interface FactorFactory {
    FactorFactory INSTANCE = new FactorFactory.Default();

    Factor createFromField(Field field);

    class Default implements FactorFactory {
      @Override
      public Factor createFromField(Field field) {
        Checks.checknotnull(field);
        Factor.Builder b = new Factor.Builder(field.getName());
        for (Object each : levelsOf(field)) {
          b.addLevel(each);
        }
        return b.build();
      }

      private static List<Object> levelsOf(Field f) {
        Checks.checknotnull(f);
        FactorField ann = f.getAnnotation(FactorField.class);
        return levelsOf(f.getName(), f.getType(), ann);
      }

      private static List<Object> levelsOf(String fieldName, final Class fieldType, FactorField ann) {
        Checks.checknotnull(fieldType);
        Checks.checknotnull(ann);
        final LevelsProvider provider = levelsProviderOf(ann);
        List<Object> ret;
        if (provider instanceof DummyLevelsProvider) {
          ret = levelsGivenByUserThroughImmediate(ann);
          if (ret == null) {
            ret = DefaultLevels.defaultLevelsOf(fieldType);
          }
        } else {
          ret = new AbstractList<Object>() {
            @Override
            public int size() {
              return provider.size();
            }

            @Override
            public Object get(int index) {
              return provider.get(index);
            }
          };
        }
        if (!ret.contains(null) && ann.includeNull()) {
          ret = Utils.newList(ret);
          ret.add(null);
        }
        List<Object> incompatibles = Utils.filter(ret, new Utils.Predicate<Object>() {
          @Override
          public boolean apply(Object in) {
            return !ReflectionUtils.isAssignable(fieldType, in);
          }
        });
        Checks.checktest(
            incompatibles.isEmpty(),
            "Incompatible values are given to field '%s': %s", fieldName, incompatibles
        );
        Checks.checktest(!ret.isEmpty(), "No levels: fieldName=%s fieldType=%s provider=%s", fieldName, fieldType, provider);
        return ret;
      }

      private static LevelsProvider levelsProviderOf(FactorField ann) {
        assert ann != null;
        //noinspection unchecked
        Plugin.Factory<LevelsProvider, Value> factory = new Plugin.Factory<LevelsProvider, Value>(
            (Class<LevelsProvider>) ann.levelsProvider(),
            new Value.Resolver()
        );
        //noinspection ConstantConditions
        return factory.create(ann.providerParams());
      }

      private static List<Object> levelsGivenByUserThroughImmediate(FactorField ann) {
        Checks.checknotnull(ann);
        List<Class> typesProvidedByUser = typesWhoseLevelsAreProvidedBy(ann);
        Checks.checktest(
            typesProvidedByUser.size() <= 1,
            "You can use only one type at once but %d were given. (%s)", typesProvidedByUser);
        if (typesProvidedByUser.size() == 0) {
          return null;
        }
        return levelsOf(ann, typesProvidedByUser.get(0));
      }

      private static List<Class> typesWhoseLevelsAreProvidedBy(FactorField ann) {
        List<Class> typesProvidedByUser = new LinkedList<Class>();
        for (Class each : ReflectionUtils.primitiveClasses()) {
          if (!DefaultLevels.defaultLevelsOf(each).equals(levelsOf(ann, each))) {
            typesProvidedByUser.add(each);
          }
        }
        if (!DefaultLevels.defaultLevelsOf(String.class).equals(levelsOf(ann, String.class))) {
          typesProvidedByUser.add(String.class);
        }
        return typesProvidedByUser;
      }

      private static List<Object> levelsOf(FactorField ann, Class<?> type) {
        Checks.checknotnull(ann);
        Checks.checknotnull(type);
        Checks.checkcond(type.isPrimitive() || String.class.equals(type) || type.isEnum(), "'%s' does not have default levels.", type);
        Object o = ReflectionUtils.invoke(ann, levelsMethodOf(type));
        Checks.checknotnull(o);
        final Object arr;
        //noinspection ConstantConditions (already checked)
        if (o.getClass().isArray()) {
          arr = o;
        } else if (o instanceof Class && ((Class) o).isEnum()) {
          ////
          // 'values' method of an enum is static and returned value is guaranteed to be an array.
          arr = ReflectionUtils.invoke(null, ReflectionUtils.getMethod(((Class) o), "values"));
        } else {
          arr = null;
        }
        Checks.checknotnull(arr).getClass();
        return new AbstractList<Object>() {
          @Override
          public Object get(int index) {
            return Array.get(arr, index);
          }

          @Override
          public int size() {
            return Array.getLength(arr);
          }
        };
      }

      private static Method levelsMethodOf(Class supportedType) {
        String lowercaseClassName = supportedType.isEnum()
            ? "enum"
            : supportedType.getSimpleName().toLowerCase();
        return ReflectionUtils.getMethod(FactorField.class, lowercaseClassName + "Levels");
      }

      public static final class DummyLevelsProvider implements LevelsProvider {
        public DummyLevelsProvider() {
        }

        @Override
        public int size() {
          return 0;
        }

        @Override
        public Object get(int n) {
          return new IllegalArgumentException();
        }
      }
    }
  }

  class DefaultLevels {
    /**
     * This field is used through reflection and necessary to hold
     * "@FactorField" annotation by which JCUnit exposes default values
     * of primitives, string, and enum.
     */
    @SuppressWarnings("unused")
    @FactorField
    public final Object defaultValues = null;

    private DefaultLevels() {
    }

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

    public static long[] longLevels() {
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

    public static List<Object> defaultLevelsOf(final Class c) {
      Checks.checknotnull(c);
      final Class supportedType;
      if (c.isPrimitive()) {
        supportedType = c;
      } else if (ReflectionUtils.isWrapper(c)) {
        supportedType = ReflectionUtils.wrapperToPrimitive(c);
      } else if (String.class.equals(c)) {
        supportedType = c;
      } else if (c.isEnum()) {
        // Note that Enum.class.isEnum should return false;
        // Note that 'values' method of an enum is static. It returns an array of the enum object.
        final Object values = ReflectionUtils.invoke(null, ReflectionUtils.getMethod(c, "values"));
        Checks.checknotnull(values);
        //noinspection ConstantConditions
        Checks.checkcond(values.getClass().isArray());
        return new AbstractList<Object>() {
          @Override
          public Object get(int index) {
            return Array.get(values, index);
          }

          @Override
          public int size() {
            return Array.getLength(values);
          }
        };
      } else if (c.getAnnotation(GenerateWith.class) != null) {
        return new AbstractList<Object>() {
          CAEngine tg = GenerateWith.CAEngineFactory.INSTANCE.createFromClass(c);

          @Override
          public Object get(int index) {
            Object ret = ReflectionUtils.create(c);
            TestCaseUtils.initializeObjectWithTuple(ret, tg.get(index));
            return ret;
          }

          @Override
          public int size() {
            return (int) tg.size();
          }
        };
      } else {
        throw new IllegalArgumentException(String.format("'%s' doesn't have default levels", c.getSimpleName()));
      }
      return FactorFactory.Default.levelsOf(get(), supportedType);
    }

    private static FactorField get() {
      return ReflectionUtils.getField(DefaultLevels.class, "defaultValues").getAnnotation(FactorField.class);
    }
  }
}
