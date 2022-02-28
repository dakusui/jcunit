package com.github.dakusui.jcunitx.utils;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.exceptions.FrameworkException;
import org.junit.runners.Parameterized;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;

import java.io.PrintStream;
import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.dakusui.jcunitx.exceptions.FrameworkException.unexpectedByDesign;
import static com.github.dakusui.jcunitx.utils.ReflectionUtils.getMethod;
import static java.util.Collections.singletonList;

/**
 * A utility class of JCUnit.
 *
 * In case there is a good library and I want to use the functionality of it in JCUnit, I
 * usually mimic it here (except {@code Preconditions} of Guava, it's in {@code Checks})
 * instead of adding dependency on it.
 *
 * This is because JCUnit's nature which should be able to be used for any other software
 * (at least as much as possible, I want to make it so).
 */
public enum Utils {
  ;

  public static PrintStream out = System.out;

  public static <T> Function<T, T> printer() {
    return printer(Object::toString);
  }

  public static <T> Function<T, T> printer(Function<T, String> formatter) {
    return t -> {
      System.out.println(formatter.apply(t));
      return t;
    };
  }

  @SuppressWarnings("unchecked")
  public static <T> T print(T data) {
    return (T) printer().apply(data);
  }

  public static <T> List<T> unique(List<T> in) {
    return new ArrayList<>(new LinkedHashSet<>(in));
  }

  public static <T> int sizeOfIntersection(Set<T> a, Set<T> b) {
    Set<T> lhs;
    Set<T> rhs;
    if (a.size() > b.size()) {
      lhs = b;
      rhs = a;
    } else {
      lhs = a;
      rhs = b;
    }
    int ret = 0;
    for (T each : lhs) {
      if (rhs.contains(each))
        ret++;
    }
    return ret;
  }

  public static AArray project(List<String> keys, AArray from) {
    AArray.Builder builder = new AArray.Builder();
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

          @SuppressWarnings("unchecked")
          @Override
          public <T extends Annotation> T getAnnotation(Class<T> annotationType) {
            FrameworkException.checkCondition(Parameterized.Parameters.class.equals(annotationType));
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

  public static String className(Class<?> klass) {
    return className(klass, "");
  }

  private static String className(Class<?> klass, String work) {
    String canonicalName = klass.getCanonicalName();
    if (canonicalName != null)
      return canonicalName;
    return className(klass.getEnclosingClass(), work + "$");
  }

  /**
   * Creates and returns an instance of a class represented by {@code TestClass}.
   *
   * @param testClass Must be validated beforehand.
   * @return created instance.
   */
  public static Object createInstanceOf(TestClass testClass) {
    try {
      return testClass.getOnlyConstructor().newInstance();
    } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
      throw unexpectedByDesign(e);
    }
  }

  /**
   * Returns a conjunction of given predicates.
   *
   * @param predicates Predicates from which a conjunction predicate is created.
   * @param <T>        A type of predicate that accepts `E`.
   * @param <E>        A type element the `predicates` accepts
   * @return A conjuntion predicate.
   */
  public static <T extends Predicate<E>, E> Predicate<E> conjunct(Iterable<T> predicates) {
    Predicate<E> ret = tuple -> true;
    for (Predicate<E> each : predicates) {
      ret = ret.and(each);
    }
    return ret;
  }

  public static <T, R> Function<T, R> memoize(Function<T, R> function) {
    Map<T, R> memo = new ConcurrentHashMap<>();
    return t -> memo.computeIfAbsent(t, function);
  }

  public static <T> T[] concatenate(T[] a, T[] b) {
    int aLen = a.length;
    int bLen = b.length;

    @SuppressWarnings("unchecked")
    T[] c = (T[]) Array.newInstance(a.getClass().getComponentType(), aLen + bLen);
    System.arraycopy(a, 0, c, 0, aLen);
    System.arraycopy(b, 0, c, aLen, bLen);

    return c;
  }

  @SafeVarargs
  public static <T> List<T> concatenate(List<T> a, T... b) {
    List<T> ret = new LinkedList<>(a);
    ret.addAll(Arrays.asList(b));
    return ret;
  }
}
