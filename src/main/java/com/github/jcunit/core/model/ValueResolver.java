package com.github.jcunit.core.model;

import com.github.jcunit.annotations.Named;
import com.github.jcunit.core.Invokable;
import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.utils.Transform;
import com.github.valid8j.pcond.core.printable.PrintableFunction;
import com.github.valid8j.pcond.forms.Printables;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;

import static com.github.valid8j.fluent.Expectations.requireArguments;
import static com.github.valid8j.fluent.Expectations.value;
import static com.github.valid8j.pcond.forms.Predicates.isEmptyString;
import static com.github.valid8j.pcond.forms.Predicates.isEqualTo;
import static java.util.Objects.requireNonNull;
import static java.util.stream.Collectors.joining;

/**
 * // @formatter:off
 * // @formatter:on
 */
public
interface ValueResolver<V> {
  static String nameOf(Method m) {
    assert m != null;
    assert m.isAnnotationPresent(Named.class);
    String value = m.getAnnotation(Named.class).value();
    return Objects.equals(Named.DEFAULT, value) ? m.getName()
                                                : value;
  }

  Optional<String> name();

  V resolve(Tuple testData);

  List<String> dependencies();

  default ValueResolver<V> name(String name) {
    return new ValueResolver<V>() {
      @Override
      public Optional<String> name() {
        return Optional.of(name);
      }

      @Override
      public V resolve(Tuple testData) {
        return ValueResolver.this.resolve(testData);
      }

      @Override
      public List<String> dependencies() {
        return ValueResolver.this.dependencies();
      }
    };
  }

  static <V> ValueResolver<V> create(String name, Function<Tuple, V> resolver, List<String> dependencies) {
    requireArguments(value(resolver).toBe().notNull(),
                     value(dependencies).toBe().notNull());
    return new ValueResolver<V>() {

      @Override
      public Optional<String> name() {
        return Optional.of(name);
      }

      @Override
      public V resolve(Tuple testData) {
        return resolver.apply(testData);
      }

      @Override
      public List<String> dependencies() {
        return new ArrayList<>(dependencies);
      }

      @Override
      public String toString() {
        return String.format("%s%s", resolver, dependencies);
      }
    };
  }

  static <V> ValueResolver<V> of(V value) {
    return from(value).$();
  }

  static <V> Builder<V> with(Function<Tuple, V> function) {
    return new Builder<>(requireNonNull(function));
  }

  static <V> Builder<V> from(V value) {
    return Builder.create(value);
  }

  static FromClass from(Class<?> klass) {
    return new FromClass(klass);
  }

  static <T> ValueResolver<T> fromInvokable(Invokable<T> invokable) {
    return new ValueResolver<T>() {
      final List<String> dependencies = invokable.parameterNames();

      @Override
      public Optional<String> name() {
        return invokable.name();
      }

      @SuppressWarnings("unchecked")
      @Override
      public T resolve(Tuple testData) {
        return invokable.invoke(dependencies.stream()
                                            .map(testData::get)
                                            .map(o -> (List<ValueResolver<?>>) o)
                                            .map(l -> l.get(0))
                                            .map(v -> (ValueResolver<?>) v)
                                            .map(r -> r.resolve(testData))
                                            .toArray());
      }

      @Override
      public List<String> dependencies() {
        return dependencies;
      }

      @Override
      public String toString() {
        return invokable.toString();
      }
    };
  }

  class Builder<V> {
    private final List<String> dependencies;
    private Function<Tuple, V> function;
    private String name;

    public Builder(String name, Function<Tuple, V> function) {
      this.dependencies = new LinkedList<>();
      this.function = requireNonNull(function);
      this.name = name != null ? name
                               : function instanceof PrintableFunction ? function.toString()
                                                                       : null;
    }

    public Builder(Function<Tuple, V> function) {
      this(null, function);
    }

    public Builder() {
      this.dependencies = new LinkedList<>();
    }

    public Builder<V> name(String name) {
      this.name = requireNonNull(name);
      return this;
    }

    public Builder<V> function(Function<Tuple, V> function) {
      this.function = requireNonNull(function);
      return this;
    }

    public Builder<V> function(String name, Function<Tuple, V> function) {
      return this.name(name).function(function);
    }

    public Builder<V> addDependency(String dependencyParameterName) {
      this.dependencies.add(requireNonNull(dependencyParameterName));
      return this;
    }

    public ValueResolver<V> build() {
      return ValueResolver.create(this.name, this.function, this.dependencies);
    }

    public ValueResolver<V> $(String... dependencies) {
      Builder<V> b = this;
      for (String each : dependencies)
        b = this.addDependency(each);
      return b.build();
    }

    public static <V> Builder<V> create(V value) {
      return with(Printables.function("value[" + value + "]", x -> value));
    }
  }

  class FromClass {
    private final Class<?> klass;

    public FromClass(Class<?> klass) {
      this.klass = klass;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public ValueResolver<Class<?>> build(String... additionalDependencies) {
      return (ValueResolver) Builder.create(this.klass).$(additionalDependencies);
    }

    public ValueResolver<Class<?>> $(String... additionalDependencies) {
      return this.build(additionalDependencies);
    }

    public static Predicate<Method> methodNameIs(String name) {
      return methodHasAnnotation(Named.class).and(Transform.$(methodGetNamedAnnotationValue()).check(isEqualTo(name))
                                                           .or(Transform.$(methodGetNamedAnnotationValue())
                                                                        .check(isEmptyString())
                                                                        .and(Transform.$(methodGetName())
                                                                                      .check(isEqualTo(name)))));
    }

    public static Predicate<Method> isStatic() {
      return Printables.predicate("isStatic", m -> Modifier.isStatic(m.getModifiers()));
    }

    public static Predicate<Method> classMethodNameIs(String name) {
      return isStatic().and(methodNameIs(name));
    }

    public static Function<Method, String> methodGetName() {
      return Printables.function("methodName", Method::getName);
    }

    public static Function<Method, String> methodGetNamedAnnotationValue() {
      return Printables.function("annotatedName", (Method m) -> m.getAnnotation(Named.class).value());
    }

    private static Predicate<Method> methodHasAnnotation(Class<? extends Annotation> annotationClass) {
      return Printables.predicate("hasAnnotation[" + annotationClass.getSimpleName() + "]",
                                  (Method m) -> m.isAnnotationPresent(annotationClass));
    }

    public static Method findMethod(Class<?> klass, Predicate<Method> query) {
      return Arrays.stream(klass.getMethods())
                   .filter(query)
                   .findFirst()
                   .orElseThrow(() -> new NoSuchElementException(String.format("No matching method for '%s' is found in '%s': %s",
                                                                               query,
                                                                               klass.getCanonicalName(),
                                                                               Arrays.stream(klass.getMethods())
                                                                                     .map(Objects::toString)
                                                                                     .collect(joining(String.format("%n- "), "[", "]")))));
    }
  }
}
