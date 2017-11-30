package com.github.dakusui.jcunit8.factorspace;

import java.util.*;

import static java.util.Arrays.asList;

/**
 * Immutable tuple.
 */
public interface Tuple {
  int indexOf(String factorName);

  Object get(int factorIndex);

  default Object get(String factorName) {
    return get(indexOf(factorName));
  }

  class Impl implements Tuple {
    final String[] keys;
    final Object[] values;
    final Object   identity;

    Impl(String[] keys, Object[] values, Object identity) {
      this.keys = keys;
      this.values = values;
      this.identity = identity;
    }

    /**
     *
     */
    public List<String> keys() {
      return asList(keys);
    }

    @Override
    public int indexOf(String factorName) {
      return Arrays.binarySearch(keys, factorName);
    }

    @Override
    public Object get(int factorIndex) {
      return values[factorIndex];
    }

    @Override
    public boolean equals(Object anotherObject) {
      //noinspection SimplifiableIfStatement
      if (anotherObject instanceof Tuple.Impl) {
        return this.identity.equals(((Impl) anotherObject).identity);
      }
      return false;
    }

    @Override
    public int hashCode() {
      return this.identity.hashCode();
    }
  }


  class Factory {
    private final FactorSpace                              factorSpace;

    public Factory(FactorSpace factorSpace) {
      this.factorSpace = factorSpace;
    }

    public Factory put(String factorName, Object value) {
      return this;
    }

    public Tuple build() {
      return null;
    }
  }
}
