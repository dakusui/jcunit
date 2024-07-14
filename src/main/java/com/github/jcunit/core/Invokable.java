package com.github.jcunit.core;

import com.github.jcunit.annotations.From;
import com.github.jcunit.factorspace.Range;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

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
  class Parameter {
    private final String name;
    private final Range range;

    Parameter(String name, Range range) {
      this.name = name;
      this.range = range;
    }

    @Override
    public String toString() {
      return String.format("%s[%s]", name, range);
    }

    public String name() {
      return this.name;
    }

    public Range range() {
      return this.range;
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

  static <T> Invokable<T> referenceTo(String parameterName, Range range) {
    return new Invokable<T>() {
      private final Parameter parameter = new Parameter(parameterName, range);

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
                                                       .map(p -> new Parameter(p.getAnnotation(From.class).value(), Range.of("0")
                                                           /*p.getAnnotation(From.class).range()*/))
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
