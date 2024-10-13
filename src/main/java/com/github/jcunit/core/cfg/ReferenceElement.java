package com.github.jcunit.core.cfg;

import static java.util.Objects.requireNonNull;

public interface ReferenceElement extends Element {

  static ReferenceElement create(String identifier) {
    requireNonNull(identifier);
    return new ReferenceElement() {
      @Override
      public <R extends ProcessingResult<R>> R accept(Processor<R> processor, R ongoingInput) {
        return processor.resolve(identifier)
                        .map(e -> e.accept(processor, ongoingInput))
                        .filter(ProcessingResult::wasSuccessful)
                        .findFirst()
                        .orElse(ongoingInput.unsuccessful());
      }

      @Override
      public Object[] signature() {
        return new Object[]{identifier};
      }

      @Override
      public String toString() {
        return stringify();
      }
    };
  }
}
