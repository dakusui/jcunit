package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.junit.platform.commons.JUnitException;
import org.junit.platform.commons.util.AnnotationUtils;

import java.lang.annotation.Annotation;
import java.lang.reflect.AnnotatedElement;
import java.lang.reflect.Method;
import java.util.function.Predicate;

import static org.junit.platform.commons.util.ReflectionUtils.HierarchyTraversalMode.BOTTOM_UP;
import static org.junit.platform.commons.util.ReflectionUtils.findMethods;

public class AnnotationConsumerInitializer {
  private AnnotationConsumerInitializer() {
    /* no-op */
  }

  // @formatter:off
  private static final Predicate<Method> isAnnotationConsumerAcceptMethod = method ->
      method.getName().equals("accept")
          && method.getParameterCount() == 1
          && method.getParameterTypes()[0].isAnnotation();
  // @formatter:on

  @SuppressWarnings({ "unchecked", "rawtypes" })
  public static <T> T initialize(AnnotatedElement annotatedElement, T instance) {
    if (instance instanceof AnnotationConsumer) {
      Method method = findMethods(instance.getClass(), isAnnotationConsumerAcceptMethod, BOTTOM_UP).get(0);
      Class<? extends Annotation> annotationType = (Class<? extends Annotation>) method.getParameterTypes()[0];
      Annotation annotation = AnnotationUtils.findAnnotation(annotatedElement, annotationType) //
          .orElseThrow(() -> new JUnitException(instance.getClass().getName()
              + " must be used with an annotation of type " + annotationType.getName()));
      initializeAnnotationConsumer((AnnotationConsumer) instance, annotation);
    }
    return instance;
  }

  private static <A extends Annotation> void initializeAnnotationConsumer(AnnotationConsumer<A> instance,
      A annotation) {
    try {
      instance.accept(annotation);
    } catch (Exception ex) {
      throw new JUnitException("Failed to initialize AnnotationConsumer: " + instance, ex);
    }
  }
}
