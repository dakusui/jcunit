package com.github.jcunit.core.model;

import com.github.jcunit.annotations.From;
import com.github.jcunit.core.tuples.Tuple;
import com.github.valid8j.pcond.forms.Printables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static com.github.valid8j.fluent.Expectations.require;
import static com.github.valid8j.fluent.Expectations.value;
import static com.github.valid8j.pcond.forms.Functions.parameter;
import static java.util.Objects.requireNonNull;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ValueResolver<V> {
  static <T> ForMethod<T> fromStaticMethod(Method method) {
    require(value(method)
                .satisfies(o -> o.invoke("getModifiers")
                                 .invokeStatic(Method.class, "isStatic", parameter())
                                 .asBoolean()
                                 .toBe()
                                 .trueValue())
                .satisfies()
                .notNull());
    List<String> arguments = Arrays.stream(method.getParameters())
                                   .filter(each -> each.isAnnotationPresent(From.class))
                                   .map(each -> each.getAnnotation(From.class))
                                   .map(From::value)
                                   .collect(Collectors.toList());
    return new ForMethod<T>() {
      @Override
      public List<String> dependencies() {
        return arguments;
      }

      @Override
      public Object[] tupleToArguments(Tuple tuple) {
        return new Object[0];
      }

      @Override
      public T invoke(Object... args) {
        try {
          return (T) method.invoke(null, args);
        } catch (IllegalAccessException | InvocationTargetException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }

  V resolve(Tuple testData);

  List<String> dependencies();

  static <V> ValueResolver<V> simple(V value) {
    return ValueResolver.fromValue(value).$();
  }

  static <V> ValueResolver<V> create(Function<Tuple, V> resolver, List<String> dependencies) {
    return with(resolver).$(dependencies.toArray(new String[0]));
  }

  static <V> Builder<V> fromValue(V value) {
    return with(Printables.function("value[" + value + "]", x -> value));
  }

  static <V> Builder<V> with(Function<Tuple, V> function) {
    return new Builder<>(requireNonNull(function));
  }


  class Builder<V> {
    private final List<String> dependencies;
    private final Function<Tuple, V> function;

    public Builder(Function<Tuple, V> function) {
      this.function = requireNonNull(function);
      this.dependencies = new LinkedList<>();
    }

    public Builder<V> addDependency(String dependencyParameterName) {
      this.dependencies.add(requireNonNull(dependencyParameterName));
      return this;
    }

    public ValueResolver<V> build() {
      return create(this.function, this.dependencies);
    }

    public ValueResolver<V> $(String... dependencies) {
      Builder<V> b = this;
      for (String each : dependencies)
        b = this.addDependency(each);
      return b.build();
    }
  }

  /**
   * // @formatter:off
   * // @formatter:on
   */
  interface ForMethod<T> extends ValueResolver<T> {
    @Override
    default T resolve(Tuple tuple) {
      return invoke(tupleToArguments(tuple));
    }

    Object[] tupleToArguments(Tuple tuple);

    T invoke(Object... args);

  }
}
