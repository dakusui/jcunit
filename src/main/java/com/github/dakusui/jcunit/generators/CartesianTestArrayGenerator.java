package com.github.dakusui.jcunit.generators;

import java.util.LinkedHashMap;

import com.github.dakusui.jcunit.core.GeneratorParameters;

public class CartesianTestArrayGenerator<T, U> extends
    BaseTestArrayGenerator<T, U> {
  @Override
  public void init(GeneratorParameters.Value[] params,
      LinkedHashMap<T, U[]> domains) {
    super.init(params, domains);
    if (this.domains == null)
      throw new NullPointerException();
    assert this.size < 0;
    assert this.cur < 0;
    size = 1;
    for (T f : this.domains.keySet()) {
      U[] d = this.domains.get(f);
      size *= d.length;
    }
    cur = 0;
  }

  @Override
  public int getIndex(T key, long cur) {
    long div = cur;
    for (T f : this.domains.keySet()) {
      U[] values = domains.get(f);
      int index = (int) (div % values.length);
      if (key.equals(f))
        return index;

      div = div / values.length;
    }
    assert false;
    return -1;
  }
}
