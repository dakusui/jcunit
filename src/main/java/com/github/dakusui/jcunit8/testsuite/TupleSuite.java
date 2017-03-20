package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.AbstractList;
import java.util.List;

public interface TupleSuite extends List<Tuple> {
  static TupleSuite fromTuples(List<Tuple> tuples) {
    class Impl extends AbstractList<Tuple> implements TupleSuite {
      @Override
      public Tuple get(int index) {
        return tuples.get(index);
      }

      @Override
      public int size() {
        return tuples.size();
      }
    }
    return new Impl();
  }
}
