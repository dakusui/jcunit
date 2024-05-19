package com.github.jcunit.core.model;

import com.github.jcunit.core.tuples.Tuple;

import java.util.List;
import java.util.function.Function;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.toList;

/**
 * // @formatter:off
 * An interface that models a specification of a parameter.
 *
 * @param <E> Execution-time parameter type
 * // @formatter:on
 */
public interface ParameterSpec<E> {
  String name();

  /**
   * Returns names of parameters on which this parameter depends.
   *
   * @return Names of parameters on which this parameter depends.
   */
  default List<String> dependencies() {
    return valueResolvers().stream()
                           .flatMap(v -> v.dependencies().stream())
                           .distinct()
                           .collect(toList());
  }

  /***
   * Returns {@link ValueResolver}s which represent values this parameter can hold.
   *
   * @return {@code ValueResolver}s.
   */
  List<ValueResolver<E>> valueResolvers();

  interface ValueResolver<V> {
    V resolve(Tuple testData);

    List<String> dependencies();

    static <V> ValueResolver<V> simple(V value) {
      return new ValueResolver<V>() {
        @Override
        public V resolve(Tuple testData) {
          return value;
        }

        @Override
        public List<String> dependencies() {
          return emptyList();
        }
      };
    }

    static <V> ValueResolver<V> reference(Function<Tuple, V> resolver, List<String> dependencies) {
      return new ValueResolver<V>() {
        @Override
        public V resolve(Tuple testData) {
          return resolver.apply(testData);
        }

        @Override
        public List<String> dependencies() {
          return dependencies;
        }
      };
    }
  }
}

