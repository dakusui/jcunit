package com.github.dakusui.jcunit.core.reflect;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.StringUtils;
import com.github.dakusui.jcunit.core.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;


public enum ReflectionUtils {
  ;

  public static List<Method> getMethods(Class<?> clazz) {
    return Utils.sort(asList(clazz.getMethods()), Comparator.comparing(Method::getName));
  }

  public static List<Field> getFields(Class<?> clazz) {
    return Utils.sort(asList(clazz.getFields()), Comparator.comparing(Field::getName));
  }

  /**
   * Returns a value of a field {@code f} from an object {@code obj}.
   * Caller must be responsible for returned value's type.
   *
   * @param obj An object from which {@code f}'s value is retrieved.
   * @param f   A field of which value is retrieved.
   * @param <T> Category of the returned value.
   */
  public static <T> T getFieldValue(Object obj, Field f) {
    try {
      //noinspection unchecked
      return (T) Checks.checknotnull(f).get(obj);
    } catch (IllegalAccessException e) {
      throw Checks.wrap(e);
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
    Class[] types = Stream.of(typedArgs).map(in -> in.type).collect(toList()).toArray(new Class[typedArgs.length]);
    try {
      return (T) clazz.getConstructor(types).newInstance(
          Stream.of(typedArgs).map(in -> in.value).toArray()
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


  public static class TypedArg {
    public final Class  type;
    public final Object value;

    public TypedArg(Class type, Object value) {
      this.type = Checks.checknotnull(type);
      this.value = value;
    }
  }
}
