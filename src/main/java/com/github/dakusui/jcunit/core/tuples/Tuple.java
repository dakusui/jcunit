package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.jcunit.core.BaseBuilder;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

public interface Tuple extends Map<String, Object>, Cloneable, Serializable {
  Tuple cloneTuple();

  class Builder implements BaseBuilder<Tuple> {
    LinkedHashMap<String, Object> attrs = new LinkedHashMap<String, Object>();
    private boolean unmodifiable;

    public Builder put(String k, Object v) {
      this.attrs.put(k, v);
      return this;
    }

    public Builder putAll(Map<String, Object> map) {
      this.attrs.putAll(map);
      return this;
    }

    public Builder setUnmodifiable(boolean unmodifiable) {
      this.unmodifiable = unmodifiable;
      return this;
    }

    @Override
    public Tuple build() {
      Tuple ret = new TupleImpl();
      for (String k : this.attrs.keySet()) {
        ret.put(k, this.attrs.get(k));
      }
      if (this.unmodifiable) {
        ret = new UnmodifiableTuple(ret);
      }
      return ret;
    }
  }

  boolean isSubtupleOf(Tuple another);
}
