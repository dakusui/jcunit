package com.github.jcunit.core;

import com.github.jcunit.annotations.From;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static com.github.jcunit.model.ValueResolver.FromClass.classMethodNameIs;
import static com.github.jcunit.model.ValueResolver.FromClass.findMethod;
import static com.github.jcunit.runners.junit5.JCUnitTestEngineUtils.nameOf;
import static java.util.Collections.singletonList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * // @formatter:on
 */
public interface Invokable<T> {
  class Parameter {
    final String name;
    final int index;

    Parameter(String name, int index) {
      this.name = name;
      this.index = index;
    }

    @Override
    public String toString() {
      return index >= 0 ? String.format("%s[%s]", name, index)
                        : name;
    }

    public String name() {
      return this.name;
    }

    public int index() {
      return this.index;
    }
  }

  default Optional<String> name() {
    return Optional.empty();
  }

  T invoke(Object... args);

  List<String> parameterNames();

  List<Parameter> parameters();

  static <T> Invokable<T> fromClassMethodNamed(Class<?> klass, String methodName) {
    return from(null, findMethod(klass, classMethodNameIs(methodName)));
  }

  static <T> Invokable<T> referenceTo(String parameterName, int index) {
    return new Invokable<T>() {
      private final Parameter parameter = new Parameter(parameterName, index);

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
      public List<Parameter> parameters() {
        return singletonList(parameter);
      }

      @Override
      public String toString() {
        return "referenceTo[" + parameter + "]";
      }
    };
  }

  static <T> Invokable<T> from(Object object, Method method) {
    return new Invokable<T>() {
      private final List<Parameter> parameters = Arrays.stream(method.getParameters())
                                                       .map(p -> new Parameter(p.getAnnotation(From.class).value(),
                                                                               p.getAnnotation(From.class).index()))
                                                       .collect(toList());
      private final String name = nameOf(method);

      @Override
      public Optional<String> name() {
        return Optional.of(name);
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
        return parameters.stream()
                         .map(p -> p.name)
                         .collect(toList());
      }

      @Override
      public List<Parameter> parameters() {
        return parameters;
      }

      @Override
      public String toString() {
        return (object == null ? ""
                               : object.getClass().getSimpleName() + ".")
               + method.getName() + parameters();
      }
    };
  }
}
