package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.GeneratorParameters;

import java.util.LinkedHashMap;

public class CartesianTestArrayGenerator<T> extends
    BaseTestArrayGenerator<T> {
  @Override
  public long initializeTestCases(GeneratorParameters.Value[] params,
      LinkedHashMap<T, Object[]> domains) {
    super.init(params, domains);
    if (this.domains == null) {
      throw new NullPointerException();
    }
    long size = 1;
    for (T f : this.domains.keySet()) {
      Object[] d = this.domains.get(f);
      size *= d.length;
    }
    return size;
  }

  @Override
  public int getIndex(T key, long cur) {
    long div = cur;
    for (T f : this.domains.keySet()) {
      Object[] values = domains.get(f);
      int index = (int) (div % values.length);
      if (key.equals(f)) {
        return index;
      }

      div = div / values.length;
    }
    assert false;
    return -1;
  }
}
