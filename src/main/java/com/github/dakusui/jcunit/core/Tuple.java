package com.github.dakusui.jcunit.core;

import java.util.LinkedHashMap;
import java.util.TreeMap;

public class Tuple extends TreeMap<String, Object> implements Cloneable {

  @Override
  public Tuple clone() {
    return (Tuple) super.clone();
  }

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
}
