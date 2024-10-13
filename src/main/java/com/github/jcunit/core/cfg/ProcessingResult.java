package com.github.jcunit.core.cfg;

import java.util.List;

import static java.util.Arrays.asList;
import static java.util.Objects.requireNonNull;

public interface ProcessingResult<R extends ProcessingResult<R>> {

  boolean wasSuccessful();

  List<Object> remainingInput();

  R successful(List<Object> remainingElements);

  R unsuccessful();

  class Default implements ProcessingResult<Default> {
    private final boolean successful;
    private final List<Object> remainingInput;


    Default(boolean successful, List<Object> remainingElements) {
      this.successful = successful;
      this.remainingInput = requireNonNull(remainingElements);
    }

    public static Default from(Object[] input) {
      return new Default(true, asList(input));
    }

    @Override
    public boolean wasSuccessful() {
      return successful;
    }

    @Override
    public List<Object> remainingInput() {
      return this.remainingInput;
    }

    @Override
    public Default successful(List<Object> remainingElements) {
      return new Default(successful, remainingElements);
    }

    @Override
    public Default unsuccessful() {
      return new Default(successful, remainingInput);
    }
  }
}
