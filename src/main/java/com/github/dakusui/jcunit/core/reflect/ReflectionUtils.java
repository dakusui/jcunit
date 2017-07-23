package com.github.dakusui.jcunit.core.reflect;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.Utils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.Comparator;
import java.util.List;

import static java.util.Arrays.asList;


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
  @SuppressWarnings("unchecked")
  public static <T> T getFieldValue(Object obj, Field f) {
    try {
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
}
