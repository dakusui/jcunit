package com.github.dakusui.jcunit.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * An annotation to tell JCUnit framework that a field is an input and the value
 * should be changed by the framework.
 * 
 * @author hiroshi
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface In {
  static enum Domain {
    /**
     * Framework should use default value set for the field.
     */
    Default,
    /**
     * Framework should use a static method whose name is the same as the
     * field's and the returned type is an array of the field's to generate the
     * values to be assigned to the field.
     */
    Method,
    /**
     * Framework doesn't assign any value the field and therefore, the value
     * assigned by user code will be used during the tests.
     */
    None
  }

  public Domain domain() default Domain.Default;

  /**
   * In case the annotated field isn't primitive, determines if
   * <code>null</code> is included in the domain. If the field's domain is not
   * <code>Default</code> this parameter will be ignored.
   * 
   * @return true - null will be included / false - null won't be included.
   */
  public boolean includeNull() default true;
}
