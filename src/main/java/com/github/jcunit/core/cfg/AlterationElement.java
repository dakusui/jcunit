package com.github.jcunit.core.cfg;

public interface AlterationElement extends Element {
  Element[] options();

  static AlterationElement create(Element... elements) {
    return new AlterationElement() {
      @Override
      public Element[] options() {
        return elements;
      }

      @Override
      public <R extends ProcessingResult<R>> R accept(Processor<R> processor, R ongoingInput) {
        R ret = ongoingInput;
        for (Element each : options()) {
          ret = each.accept(processor, ongoingInput);
        }
        return ret;
      }

      @Override
      public Object[] signature() {
        return elements;
      }

      @Override
      public String toString() {
        return stringify();
      }
    };
  }
}
