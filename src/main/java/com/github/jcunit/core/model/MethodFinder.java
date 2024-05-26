package com.github.jcunit.core.model;

import com.github.jcunit.annotations.JCUnitParameter;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Arrays;
import java.util.NoSuchElementException;
import java.util.Objects;

import static java.util.Objects.requireNonNull;

/**
 * // @formatter:off 
 * // @formatter:on 
 */
public class MethodFinder {
  private final Class<?> klass;

  public MethodFinder(Class<?> klass) {
    this.klass = requireNonNull(klass);
  }

  public static MethodFinder from(Class<?> klass) {
    return new MethodFinder(klass);
  }

  public Method classMethod(String tag) {
    return Arrays.stream(this.klass.getMethods())
                 .filter(MethodFinder::isStatic)
                 .filter(each -> each.isAnnotationPresent(JCUnitParameter.class))
                 .filter(each -> matches(each, tag))
                 .findFirst()
                 .orElseThrow(NoSuchElementException::new);
  }

  private static boolean isStatic(Method method) {
    return Modifier.isStatic(method.getModifiers());
  }

  private static boolean matches(Method method, String tag) {
    return methodTagOf(method).equals(tag);
  }

  private static String methodTagOf(Method method) {
    String methodTag = method.getAnnotation(JCUnitParameter.class).value();
    return Objects.equals(methodTag, "") ? method.getName()
                                         : methodTag;

  }
}
