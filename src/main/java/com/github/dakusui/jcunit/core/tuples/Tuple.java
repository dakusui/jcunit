package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.jcunit.core.utils.BaseBuilder;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.io.Serializable;
import java.util.*;

public interface Tuple extends Map<String, Object>, Cloneable, Serializable {
  Tuple cloneTuple();

  class Builder implements BaseBuilder<Tuple> {
    LinkedHashMap<String, Object> attrs = new LinkedHashMap<String, Object>();
    private boolean unmodifiable;
    private boolean dictionaryOrder;

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

    public Builder dictionaryOrder(boolean dictionaryOrder) {
      this.dictionaryOrder = dictionaryOrder;
      return this;
    }

    @SuppressWarnings("unchecked")
    @Override
    public Tuple build() {
      Tuple ret = new Impl();
      for (String k : this.attrs.keySet()) {
        ret.put(k, this.attrs.get(k));
      }
      if (this.unmodifiable) {
        ret = new UnmodifiableTuple(ret);
      }
      if (this.dictionaryOrder) {
        Tuple sorted = new Sorted();
        sorted.putAll(ret);
        ret = sorted;
      }
      return ret;
    }
  }

  boolean isSubtupleOf(Tuple another);

  enum Utils {
    ;
    static boolean isSubtupleOf(Tuple a, Tuple b) {
      Checks.checknotnull(a);
      Checks.checknotnull(b);
      if (!b.keySet().containsAll(a.keySet())) {
        return false;
      }
      for (String k : a.keySet()) {
        if (!com.github.dakusui.jcunit.core.utils.Utils.eq(a.get(k), b.get(k))) {
          return false;
        }
      }
      return true;
    }

  }

  class Impl extends LinkedHashMap<String, Object> implements Tuple {
    public Tuple cloneTuple() {
      return (Tuple) super.clone();
    }

    @Override public boolean isSubtupleOf(Tuple another) {
      return Utils.isSubtupleOf(this, another);
    }
  }

  class Sorted extends TreeMap<String, Object> implements Tuple {

    @Override
    public Tuple cloneTuple() {
      return (Tuple) super.clone();
    }

    @Override public boolean isSubtupleOf(Tuple another) {
      return Utils.isSubtupleOf(this, another);
    }
  }

}
