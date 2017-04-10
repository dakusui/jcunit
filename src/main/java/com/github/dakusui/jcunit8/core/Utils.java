package com.github.dakusui.jcunit8.core;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.function.Function;

import static com.github.dakusui.jcunit.core.reflect.ReflectionUtils.getMethod;
import static java.util.Collections.singletonList;

public enum Utils {
  ;

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

  public static <T> T debug(T value) {
    return debug(value, t -> t);
  }

  public static <T> T debug(T value, Function<T, Object> formatter) {
    System.out.println(formatter.apply(value));
    return value;
  }
}
