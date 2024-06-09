package com.github.jcunit.core;

import com.github.jcunit.annotations.From;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;

import static com.github.jcunit.core.model.ValueResolver.FromClass.classMethodNameIs;
import static com.github.jcunit.core.model.ValueResolver.FromClass.findMethod;
import static com.github.jcunit.runners.junit5.JCUnitTestExtensionUtils.nameOf;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * // @formatter:on
 */
public interface Invokable<T> {
  String name();

  T invoke(Object... args);

  List<String> parameterNames();

  static <T> Invokable<T> fromClassMethodNamed(Class<?> klass, String methodName) {
    return from(null, findMethod(klass, classMethodNameIs(methodName)));
  }

  static <T> Invokable<T> referenceTo(String parameterName) {
    return new Invokable<T>() {

      @Override
      public String name() {
        return parameterName;
      }

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
      private final String name = nameOf(method);

      @Override
      public String name() {
        return name;
      }

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
