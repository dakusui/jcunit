package com.github.jcunit.core.tuples;

import com.github.jcunit.exceptions.Checks;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static java.util.Arrays.asList;

public interface Tuple extends Map<String, Object>, Cloneable, Serializable {
  class Builder {
    LinkedHashMap<String, Object> attrs = new LinkedHashMap<>();

    public Builder put(String k, Object v) {
      this.attrs.put(k, v);
      return this;
    }

    public Builder putAll(Map<String, Object> map) {
      this.attrs.putAll(map);
      return this;
    }

    public Builder putRegex(String k, String... elements) {
      this.attrs.put(
          k,
          asList(elements)
      );
      return this;
    }

    public Tuple build() {
      Tuple ret = new Impl();
      for (String k : this.attrs.keySet()) {
        ret.put(k, this.attrs.get(k));
      }
      Tuple sorted = new Sorted();
      sorted.putAll(ret);
      ret = sorted;
      return ret;
    }
  }

  boolean isSubtupleOf(Tuple another);

  static Builder builder() {
    return new Builder();
  }

  enum Utils {
    ;

    static boolean isSubtupleOf(Tuple a, Tuple b) {
      Checks.checknotnull(a);
      Checks.checknotnull(b);
      if (!b.keySet().containsAll(a.keySet())) {
        return false;
      }
      for (String k : a.keySet()) {
        if (!Objects.equals(a.get(k), b.get(k))) {
          return false;
        }
      }
      return true;
    }

  }

  class Impl extends LinkedHashMap<String, Object> implements Tuple {
    @Override
    public boolean isSubtupleOf(Tuple another) {
      return Utils.isSubtupleOf(this, another);
    }
  }

  class Sorted extends TreeMap<String, Object> implements Tuple {
    @Override
    public boolean isSubtupleOf(Tuple another) {
      return Utils.isSubtupleOf(this, another);
    }
  }
}
