package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.jcunit.core.utils.Checks;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.TreeMap;

import static com.github.dakusui.pcond.Assertions.that;
import static com.github.dakusui.pcond.functions.Predicates.isNotNull;
import static java.util.Arrays.asList;

/**
 * An associative-array for JCUnit's internal data structure.
 *
 * This class was re-named from `Tuple` to `Aarray`, because the class was used both for tuples and rows.
 */
public interface Aarray extends Map<String, Object>, Cloneable, Serializable {
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

    public Aarray build() {
      Aarray ret = new Impl();
      for (String k : this.attrs.keySet()) {
        ret.put(k, this.attrs.get(k));
      }
      Aarray sorted = new Sorted();
      sorted.putAll(ret);
      ret = sorted;
      return ret;
    }
  }

  boolean isContainedBy(Aarray another);

  static Builder builder() {
    return new Builder();
  }

  enum Utils {
    ;

    static boolean isSubAarrayOf(Aarray a, Aarray b) {
      assert that(a, isNotNull());
      assert that(b, isNotNull());
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

  class Impl extends LinkedHashMap<String, Object> implements Aarray {
    @Override
    public boolean isContainedBy(Aarray another) {
      return Utils.isSubAarrayOf(this, another);
    }
  }

  class Sorted extends TreeMap<String, Object> implements Aarray {
    @Override
    public boolean isContainedBy(Aarray another) {
      return Utils.isSubAarrayOf(this, another);
    }
  }
}
