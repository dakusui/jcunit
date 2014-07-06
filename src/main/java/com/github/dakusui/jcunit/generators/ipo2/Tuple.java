package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.jcunit.core.ValueTuple;

import java.util.LinkedHashMap;

public class Tuple extends ValueTuple<String, Object> {
  public static class Builder {
    LinkedHashMap<String, Object> attrs = new LinkedHashMap<String, Object>();

    public Builder put(String k, Object v) {
      this.attrs.put(k, v);
      return this;
    }
    public Tuple build() {
      Tuple ret = new Tuple();
      for (String k : this.attrs.keySet()) {
        ret.put(k, this.attrs.get(k));
      }
      return ret;
    }
  }

  @Override
  public Tuple clone() {
    return (Tuple) super.clone();
  }
}
