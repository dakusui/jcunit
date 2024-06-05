package com.github.dakusui.jcunit8.sandbox.example;

import com.github.jcunit.annotations.From;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.github.jcunit.core.model.ValueResolver.FromClass.classMethodNamed;
import static com.github.jcunit.core.model.ValueResolver.FromClass.findMethod;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * // @formatter:on
 */
interface Invokable<T> {
  T invoke(Object... args);

  List<String> parameterNames();

  static <T> Invokable<T> fromClassMethodNamed(Class<?> klass, String methodName) {
    return from(null, findMethod(klass, classMethodNamed(methodName)));
  }

  static <T> Invokable<T> referenceTo(String parameterName) {
    return new Invokable<T>() {

      @SuppressWarnings("unchecked")
      @Override
      public T invoke(Object... args) {
        assert args.length == 1;
        return (T) args[0];
      }

      @Override
      public List<String> parameterNames() {
        return singletonList(parameterName);
      }

      @Override
      public String toString() {
        return "referenceTo[" + parameterName + "]";
      }
    };
  }

  static <T> Invokable<T> from(Object object, Method method) {
    return new Invokable<T>() {
      @SuppressWarnings("unchecked")
      @Override
      public T invoke(Object... args) {
        try {
          return (T) method.invoke(object, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }

      @Override
      public List<String> parameterNames() {
        return Arrays.stream(method.getParameters())
                     .map(p -> p.getAnnotation(From.class))
                     .map(From::value)
                     .collect(toList());
      }

      @Override
      public String toString() {
        return (object == null ? ""
                               : object.getClass().getSimpleName() + ".")
               + method.getName() + parameterNames();
      }
    };
  }
}
