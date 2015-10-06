package com.github.dakusui.jcunit.examples.fsm.simple;

import java.io.FileNotFoundException;

public class SimpleFSM {
  /**
   * This method is referred to and tested by BrokenFSMTest reflectively
   */
  @SuppressWarnings("unused")
  public boolean shouldReturnTrueButThrowsException() throws Exception {
    throw new FileNotFoundException();
  }

  /**
   * This method is referred to and tested by BrokenFSMTest reflectively
   */
  @SuppressWarnings("unused")
  public boolean shouldThrowRuntimeExceptionButFileNotFoundThrown() throws Exception {
    throw new FileNotFoundException();
  }

  /**
   * This method is referred to and tested by BrokenFSMTest reflectively
   */
  @SuppressWarnings("unused")
  public boolean shouldReturnTrueButReturnsFalse() {
    return false;
  }

  /**
   * This method is referred to and tested by BrokenFSMTest reflectively
   */
  @SuppressWarnings("unused")
  public boolean shouldThrowsExceptionButReturnsTrue() {
    return true;
  }

  /**
   * This method is referred to and tested by BrokenFSMTest reflectively
   */
  @SuppressWarnings("unused")
  public boolean valueReturningAction() {
    return true;
  }

  /**
   * This method is referred to and tested by BrokenFSMTest reflectively
   */
  @SuppressWarnings("unused")
  public boolean exceptionThrowingAction() {
    throw new RuntimeException();
  }
}
