package com.github.dakusui.jcunit8.sandbox.example;

public @interface JCUnitParameterValue {
  /**
   * A tag to specify a method to which this annotation is attached.
   * The value must be unique inside a parameter space to which the annotated method belongs.
   *
   * @return A tag for the method to be annotated by this interface.
   */
  String value();
}
