package com.github.dakusui.jcunit.annotations;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.TestCaseUtils;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.generators.TupleGenerator;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.*;

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
      "abcdefghijklmnopqrstuvwxyz", "`-=~!@#$%^&*()_+[]\\{}|;':\",./<>?", " "
  };

  TupleGeneration compositeLevels() default @TupleGeneration(
      generator = @Generator(FactorFactory.Default.DummyTupleGenerator.class),
      constraint = @Constraint(FactorFactory.Default.DummyConstraintManager.class)
  );

  Class<? extends Enum> enumLevels() default Enum.class;

  Class<? extends LevelsProvider> levelsProvider() default FactorFactory.Default.DummyLevelsProvider.class;

  /**
   * If {@code stringLevels} or {@code enumLevels} are being used, determines
   * if a {@code null} value is included in the levels.
   */
  boolean includeNull() default false;

  Param[] providerParams() default {};

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

      public static List<Object> levelsOf(Field f) {
        Checks.checknotnull(f);
        FactorField ann = f.getAnnotation(FactorField.class);
        return levelsOf(f.getName(), f.getType(), ann);
      }

      static List<Object> levelsOf(String fieldName, final Class fieldType, FactorField ann) {
        Checks.checknotnull(fieldType);
        Checks.checknotnull(ann);
        final LevelsProvider provider = levelsProviderOf(ann);
        List<Object> ret;
        if (provider instanceof DummyLevelsProvider) {
          ret = levelsProvidedByUserThroughImmediate(ann);
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
        return ret;
      }

      static LevelsProvider levelsProviderOf(FactorField ann) {
        LevelsProvider ret = ReflectionUtils.create(ann.levelsProvider());
        ret.init(ann.providerParams());
        return ret;
      }

      static List<Object> levelsProvidedByUserThroughImmediate(FactorField ann) {
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
        if (!Enum.class.equals(ann.enumLevels())) {
          typesProvidedByUser.add(Enum.class);
        }
        return typesProvidedByUser;
      }

      private static List levelsOf(FactorField ann, Class<?> type) {
        Checks.checknotnull(ann);
        Checks.checknotnull(type);
        Checks.checkcond(type.isPrimitive() || String.class.equals(type) || type.isEnum());
        Object o = ReflectionUtils.invokeMethod(ann, levelsMethodOf(type));
        Checks.checknotnull(o);
        Checks.checkcond(o.getClass().isArray() || o.getClass().isEnum());
        final Object arr;
        if (o.getClass().isArray()) {
          arr = o;
        } else if (o.getClass().isEnum()) {
          arr = ReflectionUtils.invokeMethod(o, ReflectionUtils.getMethod(o.getClass(), "values"));
        } else {
          arr = null;
        }
        Checks.checknotnull(arr);
        int l = Array.getLength(arr);
        List ret = new ArrayList(l);
        for (int i = 0; i < l; i++) {
          ret.add(Array.get(arr, i));
        }
        return ret;
      }

      private static Method levelsMethodOf(Class primitiveClass) {
        String lowercaseClassName = primitiveClass.getSimpleName().toLowerCase();
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

        @Override
        public List<String> getErrorsOnInitialization() {
          return Collections.emptyList();
        }

        @Override
        public void init(Param[] params) {
        }

        @Override
        public ParamType[] parameterTypes() {
          return new ParamType[0];
        }
      }

      public interface DummyTupleGenerator extends TupleGenerator {}
      public interface DummyConstraintManager extends ConstraintManager {}
    }
  }

  class DefaultLevels {
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

    public static List defaultLevelsOf(final Class c) {
      Checks.checknotnull(c);
      final Class primitiveClass;
      if (c.isPrimitive()) {
        primitiveClass = c;
      } else if (ReflectionUtils.isWrapper(c)) {
        primitiveClass = ReflectionUtils.wrapperToPrimitive(c);
      } else if (String.class.equals(c)) {
        primitiveClass = c;
      } else if (c.getAnnotation(TupleGeneration.class) != null) {
        return new AbstractList() {
          TupleGenerator tg = TupleGeneration.TupleGeneratorFactory.INSTANCE.createFromClass(c);

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
      return FactorFactory.Default.levelsOf(get(), primitiveClass);
    }

    private static FactorField get() {
      return ReflectionUtils.getField(DefaultLevels.class, "defaultValues").getAnnotation(FactorField.class);
    }
  }
}
