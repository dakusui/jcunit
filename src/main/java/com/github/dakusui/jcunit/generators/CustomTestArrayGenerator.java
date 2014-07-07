package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.GeneratorParameters;
import com.github.dakusui.jcunit.exceptions.JCUnitRuntimeException;

import java.util.LinkedHashMap;

public class CustomTestArrayGenerator<T> extends
    BaseTestArrayGenerator<T> {
  @Override
  public long initializeTestCases(GeneratorParameters.Value[] params,
      LinkedHashMap<T, Object[]> domains) {
    super.init(params, domains);
    if (params == null) {
      throw new NullPointerException();
    }
    if (params.length == 0) {
      throw new IllegalArgumentException(
          String.format("GeneratorParameters must have at least one value."));
    }
    return params.length;
  }

  @Override
  public int getIndex(T key, long cur) {
    // //
    // Figure out 'key' is stored at which position.
    int pos = 0;
    for (T k : this.domains.keySet()) {
      if (k == null) {
        if (key == null) {
          break;
        }
        continue;
      }
      if (k.equals(key)) {
        break;
      }
      pos++;
    }
    // //
    // Check if cur and pos are in array index boundary.
    // Also we can assume that this.params isn't null since the checking is done
    // by 'init'.
    if (cur >= this.params.length) {
      String msg = String
          .format(
              "'cur'(%d) must be positive and less than %d (maybe framework side issue)",
              cur, this.params.length);
      throw new JCUnitRuntimeException(msg, null);
    }
    // Since it's not practical to give a value more than Integer.MAX_VALUE,
    // we can naively cast it to int.
    int[] values = this.params[(int) cur].intArrayValue();
    if (pos >= values.length) {
      String msg = String
          .format(
              "The generator(%s)'s %dst/nd/th parameter(%s) has only %d element(s) but at least %d element(s) are necessary.",
              this, cur, this.params[(int) cur], values.length, pos + 1);
      throw new JCUnitRuntimeException(msg, null);
    }
    return values[pos];
  }
}
