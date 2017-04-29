package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.utils.Checks;

import java.lang.annotation.Annotation;

/**
 * An enum to hold default instances for annotations in this package.
 * This class exists because old versions of JDKs ( < 1.7.0_80) doesn't allow
 * to define constants of an enclosing type within an annotation type.
 */
public enum DefaultInstance {
  ;
  public static final Uses USES = new Uses() {
    @Override
    public Class<? extends Annotation> annotationType() {
      return Uses.class;
    }

    @Override
    public String[] value() {
      try {
        return DefaultInstance.class.getDeclaredMethod("uses").getAnnotation(Uses.class).value();
      } catch (NoSuchMethodException e) {
        throw Checks.wrap(e);
      }
    }
  };

  /**
   * A method to retrieve a default value of "@Uses" annotation
   */
  @SuppressWarnings("unused")
  @Uses
  private static void uses() {
  }
}
