package com.github.dakusui.jcunit8.exceptions;

public abstract class BaseException extends RuntimeException {
  protected BaseException(String message) {
    super(message);
  }
}
