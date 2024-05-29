package com.github.jcunit.core.model;

import com.github.jcunit.annotations.From;
import com.github.jcunit.core.tuples.Tuple;
import com.github.valid8j.pcond.forms.Printables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static com.github.valid8j.fluent.Expectations.precondition;
import static com.github.valid8j.fluent.Expectations.value;
import static com.github.valid8j.pcond.forms.Functions.parameter;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class ValueResolvers {
  public static ValueResolver.FromClass from(Class<?> klass) {
    return new ValueResolver.FromClass(klass);
  }

  public static <V> ValueResolver<V> fromStaticMethod(Method method) {
    assert precondition(value(method).satisfies(o -> o.invoke("getModifiers")
                                                      .invokeStatic(Method.class, "isStatic", parameter())
                                                      .asBoolean()
                                                      .toBe()
                                                      .trueValue())
                                     .satisfies()
                                     .notNull());
    return valueResolverFromMethod(null, method);
  }

  public static <V> ValueResolver<V> valueResolverFromMethod(Object instance, Method method) {
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

  public static <V> ValueResolver.Builder<V> _from(V value) {
    return ValueResolver.with(Printables.function("value[" + value + "]", x -> value));
  }
}
