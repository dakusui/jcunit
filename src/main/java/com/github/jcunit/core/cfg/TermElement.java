package com.github.jcunit.core.cfg;

/**
 * Represents a terminal symbol.
 */
public interface TermElement extends Element {
  Object value();

  static TermElement create(Object term) {
    return new TermElement() {
      @Override
      public Object value() {
        return term;
      }

      @Override
      public <R extends ProcessingResult<R>> R accept(Processor<R> processor, R ongoingInput) {
        return processor.before(this, ongoingInput);
      }

      @Override
      public Object[] signature() {
        return new Object[]{term};
      }

      @Override
      public String toString() {
        return stringify();
      }
    };
  }
}
