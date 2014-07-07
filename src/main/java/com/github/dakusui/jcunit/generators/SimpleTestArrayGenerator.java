package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.GeneratorParameters;

import java.util.LinkedHashMap;

public class SimpleTestArrayGenerator<T> extends
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
      size += Math.max(0, this.domains.get(f).length - 1);
    }
    return size;
  }

  @Override
  public int getIndex(T key, long cur) {
    // //
    // Initialize the returned map with the default values.
    int ret = 0;
    // //
    // If cur is 0, the returned value should always be 0.
    if (cur == 0) {
      return 0;
    }
    cur--;
    for (T f : this.domains.keySet()) {
      long index = cur;
      Object[] d = domains.get(f);
      if ((cur -= (d.length - 1)) < 0) {
        if (key.equals(f)) {
          ret = (int) (index + 1);
        }
        break;
      }
    }
    return ret;
  }
}
