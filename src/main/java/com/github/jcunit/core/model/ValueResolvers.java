package com.github.jcunit.core.model;

import com.github.jcunit.annotations.From;
import com.github.jcunit.annotations.Named;
import com.github.jcunit.core.tuples.Tuple;
import com.github.valid8j.pcond.forms.Printables;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
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

  public static <V> ValueResolver<V> fromStaticMethod(Method method) {
    assert precondition(value(method).satisfies(o -> o.invoke("getModifiers")
                                                      .invokeStatic(Modifier.class, "isStatic", parameter())
                                                      .asBoolean()
                                                      .toBe()
                                                      .trueValue())
                                     .satisfies(o -> o.toBe().notNull()));
    return valueResolverFromMethod(null, method);
  }

  public static <V> ValueResolver<V> valueResolverFromMethod(Object instance, Method method) {
    List<String> parameterNames = Arrays.stream(method.getParameters())
                                        .filter(each -> each.isAnnotationPresent(From.class))
                                        .map(each -> each.getAnnotation(From.class))
                                        .map(From::value)
                                        .collect(Collectors.toList());
    return ValueResolver.create((Tuple tuple) -> invoke(instance, method,
                                                        tupleToArguments(tuple, parameterNames)),
                                parameterNames);
  }

  private static Object[] tupleToArguments(Tuple tuple, List<String> parameterNames1) {
    return parameterNames1.stream()
                          .map(tuple::get)
                          .toArray(Object[]::new);
  }

  static <V> ValueResolver.Builder<V> _from(V value) {
    return ValueResolver.with(Printables.function("value[" + value + "]", x -> value));
  }

  @SuppressWarnings("unchecked")
  public static <V> V invoke(Object instance, Method method, Object... args) {
    try {
      return (V) method.invoke(instance, args);
    } catch (IllegalAccessException | InvocationTargetException e) {
      throw new RuntimeException(e);
    }
  }

  public static String namedOf(Method m) {
    assert m.isAnnotationPresent(Named.class);
    return ValueResolver.FromClass.annotatedName().apply(m).equals(Named.DEFAULT) ? m.getName()
                                                                                  : ValueResolver.FromClass.annotatedName().apply(m);
  }
}
