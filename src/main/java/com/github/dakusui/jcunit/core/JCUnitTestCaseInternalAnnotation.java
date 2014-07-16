package com.github.dakusui.jcunit.core;

import java.lang.annotation.Annotation;

public class JCUnitTestCaseInternalAnnotation implements Annotation {

  private final JCUnitTestCaseIdentifier id;

  public JCUnitTestCaseInternalAnnotation(JCUnitTestCaseIdentifier id) {
    Utils.checknotnull(id);
    this.id = id;
  }

  @Override
  public Class<? extends Annotation> annotationType() {
    return this.getClass();
  }

  public JCUnitTestCaseIdentifier getId() {
    return this.id;
  }
}
