package com.github.dakusui.jcunit.exceptions;

public class JCUnitUserException extends JCUnitException {
  private Class<?> targetClass;

  public JCUnitUserException(String message, Throwable t) {
    super(message, t);
  }

  public void setTargetClass(Class<?> targetClass) {
    this.targetClass = targetClass;
  }

  public Class<?> getTargetClass() {
    return targetClass;
  }

  @Override
  public String getMessage() {
    return String.format("(%s):%s", this.getTargetClass(), super.getMessage());
  }
}
