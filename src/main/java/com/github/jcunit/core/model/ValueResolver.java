package com.github.jcunit.core.model;

import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.Named;
import com.github.jcunit.core.tuples.Tuple;
import com.github.valid8j.pcond.forms.Printables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.valid8j.fluent.Expectations.precondition;
import static com.github.valid8j.fluent.Expectations.value;
import static com.github.valid8j.pcond.forms.Functions.parameter;
import static com.github.valid8j.pcond.forms.Predicates.*;
import static java.util.Objects.requireNonNull;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ValueResolver<V> {
  static FromClass from(Class<?> klass) {
    return new FromClass(klass);
  }
  static <V> ValueResolver<V> fromStaticMethod(Method method) {
    assert precondition(value(method).satisfies(o -> o.invoke("getModifiers")
                                                      .invokeStatic(Method.class, "isStatic", parameter())
                                                      .asBoolean()
                                                      .toBe()
                                                      .trueValue())
                                     .satisfies()
                                     .notNull());
    return valueResolverFromMethod(null, method);
  }

  static <V> ValueResolver<V> valueResolverFromMethod(Object instance, Method method) {
    List<String> parameterNames = Arrays.stream(method.getParameters())
                                        .filter(each -> each.isAnnotationPresent(From.class))
                                        .map(each -> each.getAnnotation(From.class))
                                        .map(From::value)
                                        .collect(Collectors.toList());
    return new ValueResolver<V>() {
      @Override
      public List<String> dependencies() {
        return parameterNames;
      }

      public Object[] tupleToArguments(Tuple tuple) {
        return parameterNames.stream()
                             .map(tuple::get)
                             .toArray(Object[]::new);
      }

      @Override
      public V resolve(Tuple tuple) {
        return invoke(tupleToArguments(tuple));
      }

      @SuppressWarnings("unchecked")
      public V invoke(Object... args) {
        try {
          return (V) method.invoke(instance, args);
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

  static <V> ValueResolver<V> of(V value) {
    return fromValue(value).$();
  }

  static <V> Builder<V> with(Function<Tuple, V> function) {
    return new Builder<>(requireNonNull(function));
  }


  class Builder<V> {
    private final List<String> dependencies;
    private Function<Tuple, V> function;

    public Builder(Function<Tuple, V> function) {
      this();
      this.function(function);
    }

    public Builder() {
      this.dependencies = new LinkedList<>();
    }

    public Builder<V> function(Function<Tuple, V> function) {
      this.function = requireNonNull(function);
      return this;
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

  class FromClass {
    private final Class<?> klass;

    public FromClass(Class<?> klass) {
      this.klass = klass;
    }

    public static Predicate<Method> named(String name) {
      return hasAnnotation().and(transform(annotatedName()).check(isEqualTo(name))
                                                           .or(transform(annotatedName()).check(isEmptyString())
                                                                                         .and(transform(methodName()).check(isEqualTo(name)))));
    }

    private static Function<Method, String> methodName() {
      return Printables.function("methodName", Method::getName);
    }

    private static Function<Method, String> annotatedName() {
      return Printables.function("annotatedName", (Method m) -> m.getAnnotation(Named.class).value());
    }

    private static Predicate<Method> hasAnnotation() {
      return Printables.predicate("hasAnnotation[" + Named.class.getSimpleName() + "]",
                                  (Method m) -> m.isAnnotationPresent(Named.class));
    }

    public <V> ValueResolver<V> classMethodNamed(String name) {
      return this.classMethod(named(name));
    }

    public <V> ValueResolver<V> classMethod(Predicate<Method> query) {
      return ValueResolver.fromStaticMethod(findMethod(this.klass, query));
    }

    private static Method findMethod(Class<?> klass, Predicate<Method> query) {
      return Arrays.stream(klass.getMethods())
                   .filter(query)
                   .findFirst()
                   .orElseThrow(() -> new NoSuchElementException(String.format("No matching method for '%s' is found in '%s': %s",
                                                                               query,
                                                                               klass.getCanonicalName(),
                                                                               Arrays.stream(klass.getMethods())
                                                                                     .map(Objects::toString)
                                                                                     .collect(Collectors.joining(String.format("%n- "), "[", "]")))));
    }
  }
}
