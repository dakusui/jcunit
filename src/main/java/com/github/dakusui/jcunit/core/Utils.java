package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.FactorField;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

public class Utils {

  public static void initializeTestObject(Object testObject,
      Tuple tuple) {
    for (String fieldName : tuple.keySet()) {
      Field f;
      f = getField(testObject, fieldName,
          FactorField.class);
      setFieldValue(testObject, f, tuple.get(fieldName));
    }
  }

  public static Field getField(Object obj, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Utils.checknotnull(obj);
    Utils.checknotnull(fieldName);
    Class<?> clazz = obj.getClass();
    return getFieldFromClass(clazz, fieldName, expectedAnnotations);
  }

  public static Field getFieldFromClass(Class<?> clazz, String fieldName,
      Class<? extends Annotation>... expectedAnnotations) {
    Utils.checknotnull(clazz);
    Utils.checknotnull(fieldName);
    Field ret;
    try {
      ret = clazz.getField(fieldName);
    } catch (SecurityException e) {
      String msg = String.format(
          "JCUnit cannot be run in this environment. (%s:%s)", e.getClass()
              .getName(), e.getMessage()
      );
      throw new JCUnitEnvironmentException(msg, e);
    } catch (NoSuchFieldException e) {
      String msg = String.format(
          "Field '%s' isn't defined in class '%s' or not annotated.",
          fieldName, clazz);
      throw new IllegalArgumentException(msg, e);
    }
    for (Class<? extends Annotation> expectedAnnotation : expectedAnnotations) {
      Utils.checknotnull(expectedAnnotation);
      if (ret.isAnnotationPresent(expectedAnnotation)) {
        return ret;
      }
    }
    throw new JCUnitException(String.format(
        "Annotated field '%s' is found in '%s, but not annotated with none of [%s]",
        fieldName, clazz, Utils.join(", ", (Object[]) expectedAnnotations)));
  }

  public static Object getFieldValue(Object obj, Field f) {
    Object ret = null;
    try {
      boolean accessible = f.isAccessible();
      try {
        f.setAccessible(true);
        ret = f.get(obj);
      } finally {
        f.setAccessible(accessible);
      }
    } catch (IllegalArgumentException e) {
      Utils.checkcond(false);
      throw e;
    } catch (IllegalAccessException e) {
      rethrow(e);
    }
    return ret;
  }

  public static void setFieldValue(Object obj, Field f, Object value) {
    Utils.checknotnull(obj);
    Utils.checknotnull(f);
    try {
      boolean accessible = f.isAccessible();
      try {
        f.setAccessible(true);
        f.set(obj, value);
      } finally {
        f.setAccessible(accessible);
      }
    } catch (IllegalArgumentException e) {
      rethrow(e);
    } catch (IllegalAccessException e) {
      rethrow(e);
    }
  }

  /**
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param sep   A separator to be used to join {@code elemes}
   * @param elems Elements to be joined.
   * @return A joined {@code String}
   */
  public static <T> String join(String sep, Formatter<T> formatter,
      T... elems) {
    Utils.checknotnull(sep);
    StringBuilder b = new StringBuilder();
    boolean firstOne = true;
    for (T s : elems) {
      if (!firstOne) {
        b.append(sep);
      }
      b.append(formatter.format(s));
      firstOne = false;
    }
    return b.toString();
  }

  public static String join(String sep, Object... elems) {
    return join(sep, Formatter.INSTANCE, elems);
  }

  /**
   * Checks if the given {@code obj} is {@code null} or not.
   * If it is, a {@code NullPointerException} will be thrown.
   * <p/>
   * This method is implemented in order to reduce dependencies on external libraries.
   *
   * @param obj A variable to be checked.
   * @param <T> The type of {@code obj}
   * @return {@code obj} itself
   */
  public static <T> T checknotnull(T obj) {
    if (obj == null) {
      throw new NullPointerException();
    }
    return obj;
  }

  public static void checkcond(boolean b) {
    if (!b) {
      throw new IllegalArgumentException();
    }
  }

  public static void checkcond(boolean b, String msg) {
    if (!b) {
      throw new IllegalArgumentException(msg);
    }
  }

  public static void rethrow(String msg, Exception e) {
    throw new JCUnitException(msg, e);
  }

  public static void rethrow(Exception e) {
    rethrow(e.getMessage(), e);
  }

  public static Field[] getAnnotatedFields(Class<?> clazz,
      Class<? extends Annotation> annClass) {
    Field[] declaerdFields = clazz.getFields();
    List<Field> ret = new ArrayList<Field>(declaerdFields.length);
    for (Field f : declaerdFields) {
      if (f.getAnnotation(annClass) != null) {
        ret.add(f);
      }
    }
    return ret.toArray(new Field[ret.size()]);
  }

  public static Tuple unmodifiableTuple(Tuple tuple) {
    return new Tuple.Builder().putAll(tuple).setUnmodifiable(true).build();
  }

  public static <T> T invokeMethod(Object on, Method m, Object... parameters) {
    try {
      return (T) m.invoke(on, parameters);
    } catch (IllegalAccessException e) {
      rethrow(e);
    } catch (InvocationTargetException e) {
      rethrow(e);
    }
    checkcond(false, "Something went wrong.");
    return null;
  }

  public static <T> T createNewInstanceUsingNoParameterConstructor(
      Class<? extends T> klazz) {
    T ret = null;
    try {
      ret = klazz.getConstructor().newInstance();
    } catch (InstantiationException e) {
      rethrow(String.format("Failed to instantiate '%s'."), e);
    } catch (IllegalAccessException e) {
      rethrow(String.format(
              "Failed to instantiate '%s'. The constructor with no parameter was not open enough.", klazz),
          e);
    } catch (InvocationTargetException e) {
      rethrow(String.format("Failed to instantiate '%s'.", klazz), e);
    } catch (NoSuchMethodException e) {
      rethrow(String.format(
          "Failed to instantiate '%s'. A constructor with no parameter was not found."),
          e);
    }
    checknotnull(ret);
    return ret;
  }

  public static <T> T getDefaultValueOfAnnotation(
      Class<? extends Annotation> klazz, String method) {
    checknotnull(klazz);
    checknotnull(method);
    try {
      return (T) klazz.getDeclaredMethod(method).getDefaultValue();
    } catch (NoSuchMethodException e) {
      rethrow(e);
    }
    checkcond(false, "Something went wrong. This line shouldn't be executed.");
    return null;
  }

  public static Object[] processParams(Param[] params) {
    Utils.checknotnull(params);
    Object[] ret = new Object[params.length];
    int i = 0;
    for (Param p : params) {
      ret[i++] = p.type().getValue(p);
    }
    return ret;
  }

  public static interface Formatter<T> {
    public static final Formatter INSTANCE = new Formatter<Object>() {
      @Override
      public String format(Object elem) {
        if (elem == null) {
          return null;
        }
        return elem.toString();
      }
    };

    public String format(T elem);
  }
}
