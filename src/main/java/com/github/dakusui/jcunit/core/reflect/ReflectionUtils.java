package com.github.dakusui.jcunit.core.reflect;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;

public class ReflectionUtils {
  private static final Class<?>[][] primitivesAndWrappers = new Class<?>[][] {
      new Class[] { boolean.class, Boolean.class },
      new Class[] { byte.class, Byte.class },
      new Class[] { char.class, Character.class },
      new Class[] { short.class, Short.class },
      new Class[] { int.class, Integer.class },
      new Class[] { long.class, Long.class },
      new Class[] { float.class, Float.class },
      new Class[] { double.class, Double.class },
  };

  private ReflectionUtils() {
  }

  public static Field getField(Class<?> clazz, String name) {
    try {
      return Checks.checknotnull(clazz).getField(Checks.checknotnull(name));
    } catch (NoSuchFieldException e) {
      String msg = String.format(
          "Field '%s' isn't defined in class '%s' or not public: canonical name='%s'",
          name,
          clazz.getSimpleName(),
          clazz.getCanonicalName());
      throw new IllegalArgumentException(msg, e);
    }
  }

  /**
   * Returns a value of a field {@code f} from an object {@code obj}.
   * Caller must be responsible for returned value's type.
   *
   * @param obj An object from which {@code f}'s value is retrieved.
   * @param f A field of which value is retrieved.
   * @param <T> Type of the returned value.
   */
  public static <T> T getFieldValue(Object obj, Field f) {
    try {
      //noinspection unchecked
      return (T) Checks.checknotnull(f).get(obj);
    } catch (IllegalAccessException e) {
      Checks.rethrow(e);
    }
    return null;
  }

  public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
    try {
      return Checks.checknotnull(clazz).getMethod(Checks.checknotnull(methodName), params);
    } catch (NoSuchMethodException e) {
      Checks.rethrow(e);
    }
    return null;
  }

  public static <T> T create(Class<T> clazz) {
    try {
      return Checks.checknotnull(clazz).newInstance();
    } catch (IllegalAccessException e) {
      Checks.rethrow(e, "A no-parameter constructor of '%s' is too less open. Make it public.", clazz);
    } catch (InstantiationException e) {
      Checks.rethrow(e, "The class '%s' couldn't be instantiated.", clazz);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      Checks.rethrow(e, "A checked exception is thrown from '%s' during instantiation.", clazz);
    }
    return null;
  }

  /**
   * Invokes a {@code method } on {@code obj} with {@code args}.
   * Caller must be responsible for checking the returned value's type.
   *
   * @param obj An object on which {@code method} is invoked.
   * @param method A {@code method} to be invoked.
   * @param args Arguments given to {@code method}.
   * @param <T> Type of returned value from {@code method}.
   */
  public static <T> T invokeMethod(Object obj, Method method, Object... args) {
    try {
      //noinspection unchecked
      return (T) Checks.checknotnull(method).invoke(obj, args);
    } catch (InvocationTargetException e) {
      Checks.rethrow(e.getTargetException(), "Failed to execute method '%s' with ", method, args);
    } catch (IllegalAccessException e) {
      Checks.rethrow(e, "A method '%s' is too less open. Make it public.", method);
    }
    return null;
  }

  public static void setFieldValue(Object obj, Field f, Object value) {
    Checks.checknotnull(obj);
    Checks.checknotnull(f);
    boolean accessible = f.isAccessible();
    try {
      f.setAccessible(true);
      f.set(obj, value);
    } catch (IllegalAccessException e) {
      // This path should never be executed since the field is set accessible.
      Checks.rethrow(e, "Something went wrong.");
    } finally {
      f.setAccessible(accessible);
    }
  }

  public static Class<?>[] primitiveClasses() {
    Class<?>[] ret = new Class[primitivesAndWrappers.length];
    for (int i = 0; i < ret.length; i++) {
      ret[i] = primitivesAndWrappers[i][0];
    }
    return ret;
  }

  public static Class<?> wrapperToPrimitive(Class<?> c) {
    Checks.checknotnull(c);
    Checks.checkcond(isWrapper(c));
    for (Class<?>[] each : primitivesAndWrappers) {
      if (each[1].equals(c)) return each[0];
    }
    assert false : "c=" + c;
    throw new RuntimeException();
  }

  public static boolean isWrapper(Class<?> c) {
    Checks.checknotnull(c);
    for (Class<?>[] each : primitivesAndWrappers) {
      if (each[1].equals(c)) return true;
    }
    return false;
  }

  public static boolean isAssignable(Class<?> to, Class<?> from) {
    Checks.checknotnull(to);
    Checks.checknotnull(from);
    return to.isAssignableFrom(from) || isWrapperOf(to, from) || isPrimitiveOf(to, from);
  }

  public static boolean isAssignable(Class<?> to, Object value) {
    Checks.checknotnull(to);
    if (value == null) {
      return !to.isPrimitive();
    }
    return isAssignable(to, value.getClass());
  }

  private static boolean isWrapperOf(Class<?> a, Class<?> b) {
    for (Class<?>[] each : primitivesAndWrappers) {
      if (Arrays.equals(each, new Class<?>[] { b, a }))
        return true;
    }
    return false;
  }

  private static boolean isPrimitiveOf(Class<?> a, Class<?> b) {
    for (Class<?>[] each : primitivesAndWrappers) {
      if (Arrays.equals(each, new Class<?>[] { a, b }))
        return true;
    }
    return false;
  }

  @SuppressWarnings("unchecked")
  public static <T> T getDefaultValueOfAnnotation(
      Class<? extends Annotation> klazz, String method) {
    Checks.checknotnull(klazz);
    Checks.checknotnull(method);
    try {
      return (T) klazz.getDeclaredMethod(method).getDefaultValue();
    } catch (NoSuchMethodException e) {
      Checks.rethrow(e);
    }
    Checks.checkcond(false, "Something went wrong. This line shouldn't be executed.");
    return null;
  }

  public static Field getField(Object obj, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Checks.checknotnull(obj);
    Checks.checknotnull(fieldName);
    Class<?> clazz = obj.getClass();
    return getFieldFromClass(clazz, fieldName, expectedAnnotations);
  }

  public static Field getFieldFromClass(Class<?> clazz, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Checks.checknotnull(clazz);
    Checks.checknotnull(fieldName);
    Field ret = getField(clazz, fieldName);
    if (expectedAnnotations.length > 0) {
      for (Class<? extends Annotation> expectedAnnotation : expectedAnnotations) {
        Checks.checknotnull(expectedAnnotation);
        if (ret.isAnnotationPresent(expectedAnnotation)) {
          return ret;
        }
      }
      Checks.checkparam(false,
          String.format(
              "Field '%s' is found in '%s, but not annotated with none of [%s]",
              fieldName,
              clazz,
              Utils.join(",", new Utils.Formatter<Class<? extends Annotation>>() {
                    @Override
                    public String format(Class<? extends Annotation> elem) {
                      return elem.getSimpleName();
                    }
                  },
                  expectedAnnotations)
          )
      );
    }
    return ret;
  }
}
