package com.github.dakusui.jcunit.plugins.caengines;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.AbstractList;
import java.util.List;

public interface CoveringArray extends List<Tuple> {
  /**
   * Returns a tuple which represents a test case identified by {@code elementId}
   */
  Tuple get(int elementId);

  /**
   * Returns next valid id.
   * If {@code elementId} reaches the end, {@code -1} will be returned.
   */
  int nextId(int testId);

  /**
   * Returns the first valid id in the array.
   */
  int firstId();

  /**
   * Returns total number of test cases generated by the implementations of this interface.
   */
  int size();

  class Base extends AbstractList<Tuple> implements CoveringArray {
    private final List<Tuple> elements;

    public Base(List<Tuple> elements) {
      this.elements = Checks.checknotnull(elements);
    }

    @Override
    public Tuple get(int elementId) {
      return elements.get(elementId);
    }

    @Override
    public int nextId(int elementId) {
      return (++elementId < this.size()) ? elementId : -1;
    }

    @Override
    public int firstId() {
      return 0;
    }

    @Override
    public int size() {
      return elements.size();
    }
  }
}