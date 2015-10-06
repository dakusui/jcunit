package com.github.dakusui.jcunit.examples.fsm.brokenfsm;

import java.io.FileNotFoundException;

public class BrokenFSM {
  public boolean shouldReturnTrueButThrowsException() throws FileNotFoundException {
    throw new FileNotFoundException();
  }

  public boolean shouldReturnTrueButReturnsFalse() {
    return false;
  }

  public boolean shouldThrowsExceptionButReturnsTrue() {
    return true;
  }
}
