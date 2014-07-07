package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.Out;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.ObjectUnderFrameworkException;

import java.lang.annotation.Annotation;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Utils {
  public static BigDecimal bigDecimal(Number num) {
    if (num == null) {
      throw new NullPointerException();
    }
    if (num instanceof BigDecimal) {
      return (BigDecimal) num;
    }
    if (num instanceof BigInteger) {
      return new BigDecimal((BigInteger) num);
    }
    if (num instanceof Byte) {
      return new BigDecimal((Byte) num);
    }
    if (num instanceof Double) {
      return new BigDecimal((Double) num);
    }
    if (num instanceof Float) {
      return new BigDecimal((Float) num);
    }
    if (num instanceof Integer) {
      return new BigDecimal((Integer) num);
    }
    if (num instanceof Long) {
      return new BigDecimal((Long) num);
    }
    if (num instanceof Short) {
      return new BigDecimal((Short) num);
    }
    String message = String.format(
        "Unsupported number object %s(%s) is given.", num, num.getClass());
    throw new IllegalArgumentException(message);
  }

  public static Object normalize(Object v) {
    if (v == null) {
      return null;
    }
    if (v instanceof Number) {
      return bigDecimal((Number) v);
    }
    return v;
  }

  public static Field getField(Object obj, String fieldName) {
    if (obj == null) {
      throw new NullPointerException();
    }
    Class<?> clazz = obj.getClass();
    Field ret = getFieldFromClass(clazz, fieldName);
    return ret;
  }

  public static Field getFieldFromClass(Class<?> clazz, String fieldName) {
    Field ret;
    try {
      ret = clazz.getField(fieldName);
      if (!ret.isAnnotationPresent(In.class)
          && !ret.isAnnotationPresent(Out.class)) {
        throw new NoSuchFieldException();
      }
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
    return ret;
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
      assert false;
      throw e;
    } catch (IllegalAccessException e) {
      assert false;
    }
    return ret;
  }

  public static void setFieldValue(Object obj, Field f, Object value) {
    try {
      boolean accessible = f.isAccessible();
      try {
        f.setAccessible(true);
        f.set(obj, value);
      } finally {
        f.setAccessible(accessible);
      }
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (IllegalAccessException e) {
      assert false;
    }
  }

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

  public static void initializeTestObject(Object out,
      Map<Field, Object> values) {
    for (Field f : values.keySet()) {
      setFieldValue(out, f, values.get(f));
    }
  }

  public static Field[] getInFieldsFromClassUnderTest(Class<?> cut) {
    return getAnnotatedFieldsFromClassUnderTest(cut, In.class);
  }

  public static Field[] getOutFieldsFromClassUnderTest(Class<?> cut) {
    return getAnnotatedFieldsFromClassUnderTest(cut, Out.class);
  }

  public static Field[] getAnnotatedFieldsFromClassUnderTest(Class<?> cut,
      Class<? extends Annotation> annClass) {
    Field[] declaerdFields = cut.getFields();
    List<Field> ret = new ArrayList<Field>(declaerdFields.length);
    for (Field f : declaerdFields) {
      if (f.getAnnotation(annClass) != null) {
        ret.add(f);
      }
    }
    return ret.toArray(new Field[0]);
  }

  public static Object[] invokeDomainMethod(Method m) throws JCUnitException {
    Object[] ret = null;
    boolean accessible = m.isAccessible();
    try {
      m.setAccessible(true);
      Object tmp = m.invoke(null);
      int arrayLength = Array.getLength(tmp);
      ret = new Object[arrayLength];
      for (int i = 0; i < arrayLength; i++) {
        ret[i] = Array.get(tmp, i);
      }
    } catch (IllegalArgumentException e) {
      assert false;
    } catch (IllegalAccessException e) {
      assert false;
    } catch (InvocationTargetException e) {
      Throwable t = e.getTargetException();
      try {
        throw t;
      } catch (Error ee) {
        throw ee;
      } catch (RuntimeException ee) {
        throw ee;
      } catch (JCUnitException ee) {
        throw ee;
      } catch (Throwable tt) {
        String message = String.format(
            "A domain method '%s' reported an internal exception (%s)", m,
            tt.getMessage());
        throw new ObjectUnderFrameworkException(message, tt);
      }
    } finally {
      m.setAccessible(accessible);
    }
    return ret;
  }

  @SuppressWarnings("unchecked")
  public static <T> T cast(Class<T> clazz, Object obj) {
    if (clazz == null) {
      throw new NullPointerException();
    }
    if (obj == null || clazz.isAssignableFrom(obj.getClass())) {
      return (T) obj;
    }
    throw new ClassCastException(msgClassCastException(clazz, obj));
  }

  private static String msgClassCastException(Class<?> clazz, Object obj) {
    return String.format(
        "An instance of '%s' class is expected, but '%s'(class=%s) was given.",
        clazz.getName(), obj, obj == null ? null : obj.getClass().getName());
  }

  private static final Pattern methodPattern = Pattern
      .compile("[a-zA-Z$_][0-9a-zA-Z$_]*");

  public static Object invokeMethod(Object obj, String methodId,
      Object[] params)
      throws JCUnitException {
    if (obj == null) {
      throw new NullPointerException();
    }
    try {
      Method m = obj.getClass().getMethod(getMethodName(methodId),
          getParameterTypes(methodId));
      return m.invoke(obj, params);
    } catch (IllegalArgumentException e) {
      throw e;
    } catch (IllegalAccessException e) {
      throw new JCUnitException(e.getMessage(), e);
    } catch (InvocationTargetException e) {
      throw new JCUnitException(e.getMessage(), e.getCause());
    } catch (SecurityException e) {
      throw new JCUnitException(e.getMessage(), e);
    } catch (NoSuchMethodException e) {
      throw new JCUnitException(e.getMessage(), e);
    }
  }

  private static Class<?>[] getParameterTypes(String methodId) {
    return new Class<?>[] { };
  }

  private static String getMethodName(String methodId) throws JCUnitException {
    Matcher m = methodPattern.matcher(methodId);
    if (m.find()) {
      return m.group(0);
    }
    throw new JCUnitException(String.format("Specified method wasn't found:%s",
        methodId), null);
  }

  public static void main(String[] args) {
    System.out.println(methodPattern.matcher("getImage").group(0));
  }
}
