package com.github.jcunit.core.cfg;

public interface ConcatenatedElement extends Element {
  Element[] segments();

  static ConcatenatedElement create(Element... elements) {
    return new Impl(elements);
  }

  final class Impl implements ConcatenatedElement {
    private final Element[] elements;

    public Impl(Element... elements) {
      this.elements = elements;
    }

    @Override
    public Element[] segments() {
      return elements;
    }

    @Override
    public <R extends ProcessingResult<R>> R accept(Processor<R> processor, R ongoingInput) {
      R processingResult = ongoingInput;
      for (Element each : segments()) {
        processingResult = each.accept(processor, ongoingInput);
      }
      return processingResult;
    }

    @Override
    public Object[] signature() {
      return elements;
    }

    @Override
    public int hashCode() {
      return stringify().hashCode();
    }

    @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
    @Override
    public boolean equals(Object o) {
      return isEqualTo(o);
    }

    @Override
    public String toString() {
      return stringify();
    }
  }
}
