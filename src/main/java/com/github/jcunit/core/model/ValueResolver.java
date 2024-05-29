package com.github.jcunit.core.model;

import com.github.jcunit.annotations.Named;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.utils.Transform;
import com.github.valid8j.pcond.forms.Printables;

import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static com.github.valid8j.pcond.forms.Predicates.isEmptyString;
import static com.github.valid8j.pcond.forms.Predicates.isEqualTo;
import static java.util.Objects.requireNonNull;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ValueResolver<V> {

  V resolve(Tuple testData);

  List<String> dependencies();

  static <V> ValueResolver<V> simple(V value) {
    return ValueResolver.from(value).$();
  }

  static <V> ValueResolver<V> create(Function<Tuple, V> resolver, List<String> dependencies) {
    return with(resolver).$(dependencies.toArray(new String[0]));
  }

  static <V> Builder<V> from(V value) {
    return ValueResolvers._from(value);
  }

  static <V> ValueResolver<V> of(V value) {
    return from(value).$();
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

    public <V> ValueResolver<V> classMethodNamed(String name) {
      return this.classMethod(named(name));
    }

    public <V> ValueResolver<V> classMethod(Predicate<Method> query) {
      return ValueResolvers.fromStaticMethod(findMethod(this.klass, query));
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ValueResolver<Class<?>> build(String... additionalDependencies) {
      return (ValueResolver) ValueResolvers._from(this.klass).$(additionalDependencies);
    }

    public static Predicate<Method> named(String name) {
      return hasAnnotation().and(Transform.$(annotatedName())
                                          .check(isEqualTo(name))
                                          .or(Transform.$(annotatedName())
                                                       .check(isEmptyString())
                                                       .and(Transform.$(methodName())
                                                                     .check(isEqualTo(name)))));
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
