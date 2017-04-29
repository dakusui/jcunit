package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getMethod;
import static com.github.dakusui.jcunit8.exceptions.FrameworkException.unexpectedByDesign;
import static java.util.Collections.singletonList;

public enum Utils {
  ;

  // TODO: Move somewhere else more appropriate
  public static final Object DontCare = new Object() {
    @Override
    public String toString() {
      return "D/C";
    }
  };
  // TODO: Move somewhere else more appropriate
  public static final Object VOID     = new Object() {
    @Override
    public String toString() {
      return "(VOID)";
    }
  };

  public static <T> Function<T, T> printer() {
    return printer(Object::toString);
  }

  public static <T> Function<T, T> printer(Function<T, String> formatter) {
    return t -> {
      System.out.println(formatter.apply(t));
      return t;
    };
  }

  public static <T> T print(T data) {
    //noinspection unchecked
    return (T) printer().apply(data);
  }

  public static <T> List<T> unique(List<T> in) {
    return new ArrayList<>(new LinkedHashSet<>(in));
  }

  public static Tuple project(List<String> keys, Tuple from) {
    Tuple.Builder builder = new Tuple.Builder();
    keys.forEach((String key) -> builder.put(key, from.get(key)));
    return builder.build();
  }

  public static TestClass createTestClassMock(final TestClass testClass) {
    return new TestClass(testClass.getJavaClass()) {
      @Override
      public List<FrameworkMethod> getAnnotatedMethods(final Class<? extends Annotation> annClass) {
        if (Parameterized.Parameters.class.equals(annClass)) {
          return singletonList(createDummyFrameworkMethod());
        }
        return super.getAnnotatedMethods(annClass);
      }

      private FrameworkMethod createDummyFrameworkMethod() {
        return new FrameworkMethod(getDummyMethod()) {
          public boolean isStatic() {
            return true;
          }

          @Override
          public Object invokeExplosively(Object target, Object... params) {
            return new Object[] {};
          }

          @Override
          public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            FrameworkException.checkCondition(Parameterized.Parameters.class.equals(annotationType));
            //noinspection unchecked
            return (T) new Parameterized.Parameters() {
              @Override
              public Class<? extends Annotation> annotationType() {
                return Parameterized.Parameters.class;
              }

              @Override
              public String name() {
                return "{index}";
              }
            };
          }
        };
      }

      private Method getDummyMethod() {
        return getToStringMethod(Object.class);
      }
    };
  }

  private static Method getToStringMethod(Class<?> klass) {
    return getMethod(klass, "toString");
  }

  public static String className(Class klass) {
    return className(klass, "");
  }

  private static String className(Class klass, String work) {
    String canonicalName = klass.getCanonicalName();
    if (canonicalName != null)
      return canonicalName;
    return className(klass.getEnclosingClass(), work + "$");
  }

  /**
   * @param testClass Must be validated beforehand.
   */
  public static Object createInstanceOf(TestClass testClass) {
    try {
      return testClass.getOnlyConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw unexpectedByDesign(e);
    }
  }
}
