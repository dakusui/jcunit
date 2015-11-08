package com.github.dakusui.jcunit.core.reflect;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.StringUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.*;


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

  public static List<Method> getMethods(Class<?> clazz) {
    return Utils.sort(Utils.asList(clazz.getMethods()), BY_MEMBER_NAME);
  }

  public static List<Field> getFields(Class<?> clazz) {
    return Utils.sort(Utils.asList(clazz.getFields()), BY_MEMBER_NAME);
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
   * Internally does {@code Class#getDeclaredField}. To be used with {@code getFieldValueForcibly}.
   */
  public static Field getFieldDeclaredIn(Class<?> clazz, String name) {
    try {
      return Checks.checknotnull(clazz).getDeclaredField(Checks.checknotnull(name));
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
   * @param f   A field of which value is retrieved.
   * @param <T> Type of the returned value.
   */
  public static <T> T getFieldValue(Object obj, Field f) {
    try {
      //noinspection unchecked
      return (T) Checks.checknotnull(f).get(obj);
    } catch (IllegalAccessException e) {
      throw Checks.wrap(e);
    }
  }

  public static <T> T getFieldValueForcibly(Object obj, Field f) {
    Checks.checknotnull(f).setAccessible(true);
    try {
      return getFieldValue(obj, f);
    } finally {
      f.setAccessible(false);
    }
  }

  public static Method getMethod(Class<?> clazz, String methodName, Class<?>... params) {
    try {
      return Checks.checknotnull(clazz).getMethod(Checks.checknotnull(methodName), params);
    } catch (NoSuchMethodException e) {
      throw Checks.wrap(e);
    }
  }

  public static <T> T create(Class<T> clazz, TypedArg... typedArgs) {
    Checks.checknotnull(clazz);
    Class[] types = Utils.transform(typedArgs, new Utils.Form<TypedArg, Class>() {
      @Override
      public Class apply(TypedArg in) {
        return in.type;
      }
    }).toArray(new Class[typedArgs.length]);
    try {
      return clazz.getConstructor(types).newInstance(
          Utils.transform(typedArgs, new Utils.Form<TypedArg, Object>() {
            @Override
            public Object apply(TypedArg in) {
              return in.value;
            }
          }).toArray()
      );
    } catch (NoSuchMethodException e) {
      throw Checks.wrap(e,
          "Failed to find a constructor in '%s' which matches %s",
          StringUtils.toString(clazz),
          Arrays.toString(types)
      );
    } catch (InvocationTargetException e) {
      throw Checks.wrap(e.getTargetException(),
          "An exception thrown during instantiation of '%s'", clazz);
    } catch (InstantiationException e) {
      throw Checks.wrap(e,
          "Instantiation of '%s' is failed.", clazz);
    } catch (IllegalAccessException e) {
      throw Checks.wrap(e,
          "An exception thrown during instantiation of '%s'", clazz);
    }
  }

  public static <T> T create(Class<T> clazz) {
    try {
      return Checks.checknotnull(clazz).newInstance();
    } catch (IllegalAccessException e) {
      throw Checks.wrap(e, "A no-parameter constructor of '%s' is too less open. Make it public.", clazz);
    } catch (InstantiationException e) {
      throw Checks.wrap(e, "The class '%s' couldn't be instantiated.", clazz);
    } catch (RuntimeException e) {
      throw e;
    } catch (Exception e) {
      throw Checks.wrap(e, "A checked exception is thrown from '%s' during instantiation.", clazz);
    }
  }

  /**
   * Invokes a {@code method } on {@code obj} with {@code args}.
   * Caller must be responsible for checking the returned value's type.
   *
   * @param obj    An object on which {@code method} is invoked.
   * @param method A {@code method} to be invoked.
   * @param args   Arguments given to {@code method}.
   * @param <T>    Type of returned value from {@code method}.
   */
  public static <T> T invoke(Object obj, Method method, Object... args) {
    try {
      //noinspection unchecked
      return (T) Checks.checknotnull(method).invoke(obj, args);
    } catch (InvocationTargetException e) {
      throw Checks.wrap(e.getTargetException(), "Failed to execute method '%s' with ", method, args);
    } catch (IllegalAccessException e) {
      throw Checks.wrap(e, "A method '%s' is too less open. Make it public.", method);
    }
  }

  public static <T> T invokeForcibly(Object obj, Method method, Object... args) {
    method.setAccessible(true);
    try {
      return invoke(obj, method, args);
    } finally {
      method.setAccessible(false);
    }
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
      throw Checks.wrap(e, "Something went wrong.");
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
      if (each[1].equals(c))
        return each[0];
    }
    assert false : "c=" + c;
    throw new RuntimeException();
  }

  public static boolean isWrapper(Class<?> c) {
    Checks.checknotnull(c);
    for (Class<?>[] each : primitivesAndWrappers) {
      if (each[1].equals(c))
        return true;
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
      throw Checks.wrap(e);
    }
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
              StringUtils.join(",", new StringUtils.Formatter<Class<? extends Annotation>>() {
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

  public static Field[] getAnnotatedFields(Class<?> clazz,
      Class<? extends Annotation> annClass) {
    List<Field> fields = getFields(clazz);
    List<Field> ret = new ArrayList<Field>(fields.size());
    for (Field f : fields) {
      if (f.getAnnotation(annClass) != null) {
        ret.add(f);
      }
    }
    Collections.sort(ret, new Comparator<Field>() {
      @Override
      public int compare(Field o1, Field o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    return ret.toArray(new Field[ret.size()]);
  }

  public static class TypedArg {
    public final Class  type;
    public final Object value;

    public TypedArg(Class type, Object value) {
      this.type = Checks.checknotnull(type);
      this.value = value;
    }
  }

  public static final Utils.By<Member> BY_MEMBER_NAME = new Utils.By<Member>() {
    @Override
    public Comparable apply(Member in) {
      return in.getName();
    }
  };
}
