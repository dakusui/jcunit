package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArray;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.TestCaseUtils;
import org.junit.runners.model.FrameworkField;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.*;
import java.util.AbstractList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.utils.Checks.checknotnull;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@ValidateWith(FactorField.Validator.class)
public @interface FactorField {
  @interface Source {
    enum Type {
      STATIC_METHOD {
        public List<Object> getLevels(String memberName, Class testClass) {
          List<Object> ret = new LinkedList<Object>();
          ////
          // It's guaranteed that the value returned by the target method is an array of
          // compatible type.
          Object arr = ReflectionUtils.invoke(null,
              ReflectionUtils.getMethod(testClass, memberName)
              );
          for (int i = 0; i < Array.getLength(arr); i++) {
            ret.add(Array.get(arr, i));
          }
          return ret;
        }

        public List<Exception> validateClassToWhichAnnotatedFieldBelongs(Source source, FrameworkField field) {
          Class<?> enclosingClass = checknotnull(field).getDeclaringClass();
          List<Exception> ret = new LinkedList<Exception>();
          try {
            Method m = enclosingClass.getMethod(source.value());
            checkModifiers(m, ret);
            checkParameters(m, ret);
            checkType(m, field.getType(), m.getReturnType(), ret);
          } catch (NoSuchMethodException e) {
            ret.add(new Exception(String.format(
                "The class '%s' must have a static public method '%s', but no such a method."
                , enclosingClass.getCanonicalName()
                , source.value()
            )));
          }
          return ret;
        }
      },
      STATIC_FIELD {
        public List<Object> getLevels(String memberName, Class testClass) {
          List<Object> ret = new LinkedList<Object>();
          ////
          // It's guaranteed that the value of the field is an array of
          // compatible type.
          Object arr = ReflectionUtils.getFieldValue(null, ReflectionUtils.getField(testClass, memberName));
          for (int i = 0; i < Array.getLength(arr); i++) {
            ret.add(Array.get(arr, i));
          }
          return ret;
        }

        public List<Exception> validateClassToWhichAnnotatedFieldBelongs(Source source, FrameworkField field) {
          Class<?> enclosingClass = checknotnull(field).getDeclaringClass();
          List<Exception> ret = new LinkedList<Exception>();
          try {
            Field f = enclosingClass.getField(source.value());
            checkModifiers(f, ret);
            checkType(f, field.getType(), f.getType(), ret);
          } catch (NoSuchFieldException e) {
            ret.add(new Exception(String.format(
                "The class '%s' must have a static public field '%s', but no such a field."
                , enclosingClass.getCanonicalName()
                , source.value()
            )));
          }
          return ret;
        }
      };

      public abstract List<Object> getLevels(String memberName, Class o);

      public abstract List<Exception> validateClassToWhichAnnotatedFieldBelongs(Source source, FrameworkField field);

      static private void checkParameters(Method m, List<Exception> errors) {
        if (m.getParameterTypes().length != 0) {
          errors.add(new Exception(String.format(
              "'%s' method in '%s' must not have any parameters (%s)"
              , m.getName()
              , m.getDeclaringClass().getCanonicalName()
              , Arrays.toString(m.getParameterTypes())
          )));
        }
      }

      static private void checkType(Member member, Class<?> expectedComponentType, Class<?> actualMemberType, List<Exception> errors) {
        String enclosingClassName = member.getDeclaringClass().getCanonicalName();
        String memberName = member.getName();
        String memberType = member.getClass().getSimpleName().toLowerCase();
        if (!actualMemberType.isArray()) {
          errors.add(new Exception(String.format(
              "'%s' %s in %s must be an array, but not (%s)"
              , memberName
              , memberType
              , enclosingClassName
              , actualMemberType.getCanonicalName()
          )));
        } else {
          if (!expectedComponentType.isAssignableFrom(actualMemberType.getComponentType())) {
            errors.add(new Exception(String.format(
                "'%s' %s in %s must be an array whose component type is compatible with '%s' but not (%s)"
                , memberName
                , memberType
                , enclosingClassName
                , expectedComponentType.getCanonicalName()
                , actualMemberType.getCanonicalName()
            )));
          }
        }
      }

      static private void checkModifiers(Member member, List<Exception> errors) {
        int modifiers = member.getModifiers();
        String enclosingClassName = member.getDeclaringClass().getCanonicalName();
        String memberName = member.getName();
        String memberType = member.getClass().getSimpleName().toLowerCase();
        if (!Modifier.isPublic(modifiers)) {
          errors.add(new Exception(String.format(
              "'%s' %s declared in '%s' must be a static public. But not public.", memberName, memberType, enclosingClassName
          )));
        }
        if (!Modifier.isStatic(modifiers)) {
          errors.add(new Exception(String.format(
              "'%s' %s declared in '%s' must be a static public. But not static.", memberName, memberType, enclosingClassName
          )));
        }
      }
    }

    Type type() default Type.STATIC_METHOD;

    String value();
  }

  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedField(FrameworkField field) {
      // TODO: Issue-#45
      List<Exception> ret = new LinkedList<Exception>();
      Source sources[] = field.getAnnotation(FactorField.class).from();
      for (Source each : sources) {
        ret.addAll(each.type().validateClassToWhichAnnotatedFieldBelongs(each, field));
      }
      return ret;
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

  Source[] from() default {};

  Class<? extends LevelsProvider> levelsProvider() default FactorFactory.Default.DummyLevelsProvider.class;

  /**
   * If {@code stringLevels} or {@code enumLevels} are being used, determines
   * if a {@code null} value is included in the levels.
   */
  boolean includeNull() default false;

  Value[] args() default {};

  interface FactorFactory {
    FactorFactory INSTANCE = new FactorFactory.Default();

    Factor createFromField(Field field);

    class Default implements FactorFactory {
      @Override
      public Factor createFromField(Field field) {
        checknotnull(field);
        Factor.Builder b = new Factor.Builder(field.getName());
        for (Object each : levelsOf(field)) {
          b.addLevel(each);
        }
        return b.build();
      }

      private static List<Object> levelsOf(Field f) {
        checknotnull(f);
        FactorField ann = f.getAnnotation(FactorField.class);
        return levelsOf(f.getName(), f.getType(), ann);
      }

      private static List<Object> levelsOf(String fieldName, final Class fieldType, FactorField ann) {
        checknotnull(fieldType);
        checknotnull(ann);
        RunnerContext runnerContext = new RunnerContext.Base(fieldType);
        final LevelsProvider provider = new LevelsProvider.FromFactorField(ann, runnerContext).build();
        List<Object> ret;
        if (provider instanceof DummyLevelsProvider) {
          ret = levelsGivenByUserDirectly(ann, runnerContext);
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

      public static List<Object> levelsGivenByUserDirectly(FactorField ann, RunnerContext runnerContext) {
        checknotnull(ann);
        checknotnull(runnerContext);
        List<Class> typesProvidedByUser = typesWhoseLevelsAreProvidedBy(ann);
        Checks.checktest(
            typesProvidedByUser.size() <= 1,
            "You can use only one type at once but %d were given.", typesProvidedByUser.size());
        if (typesProvidedByUser.size() == 0) {
          if (ann.from().length > 0) {
            Class testClass = runnerContext.get(RunnerContext.Key.TEST_CLASS);
            List<Object> ret = new LinkedList<Object>();
            for (Source each : ann.from()) {
              ret.addAll(each.type().getLevels(each.value(), testClass));
            }
            return ret;
          }
          return null;
        }
        Checks.checktest(ann.from().length == 0,
            "You cannot use 'from' attribute of '@%s' when you are explicitly specifying levels."
            , ann.getClass().getCanonicalName()
        );
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
        checknotnull(ann);
        checknotnull(type);
        Checks.checkcond(type.isPrimitive() || String.class.equals(type) || type.isEnum(), "'%s' does not have default levels.", type);
        Object o = ReflectionUtils.invoke(ann, levelsMethodOf(type));
        checknotnull(o);
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
        checknotnull(arr).getClass();
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

      public static final class DummyLevelsProvider extends LevelsProvider.Base implements LevelsProvider {
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
      checknotnull(c);
      final Class supportedType;
      final GenerateCoveringArrayWith ann;
      if (c.isPrimitive()) {
        supportedType = c;
      } else if (ReflectionUtils.isWrapper(c)) {
        supportedType = ReflectionUtils.wrapperToPrimitive(c);
      } else if (String.class.equals(c)) {
        supportedType = c;
      } else if (c.isEnum()) {
        // Note that Enum.class.isEnum should return false;
        return Arrays.asList(c.getEnumConstants());
      } else if ((ann = (GenerateCoveringArrayWith) c.getAnnotation(GenerateCoveringArrayWith.class)) != null) {
        return new AbstractList<Object>() {
          FactorSpace factorSpace = new FactorSpace.Builder()
              .addFactorDefs(JCUnit.getFactorDefsFrom(c))
              .setTopLevelConstraintChecker(new ConstraintChecker.Builder(ann.checker(), RunnerContext.DUMMY).build())
              .build();
          CoveringArray ca = new CoveringArrayEngine.FromAnnotation(JCUnit.getGenerator(c), RunnerContext.DUMMY).build().generate(factorSpace);

          @Override
          public Object get(int index) {
            Object ret = ReflectionUtils.create(c);
            TestCaseUtils.initializeObjectWithTuple(ret, ca.get(index));
            return ret;
          }

          @Override
          public int size() {
            return ca.size();
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
