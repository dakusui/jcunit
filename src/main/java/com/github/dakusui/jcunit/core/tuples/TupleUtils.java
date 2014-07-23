package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.enumerator.CartesianEnumeratorAdaptor;
import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.Domains;
import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.jcunit.core.LabeledTestCase;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;

import java.io.Serializable;
import java.util.*;

public class TupleUtils {
	public static class CartesianTuples extends CartesianEnumeratorAdaptor<Tuple, String, Object> {

    private final Tuple base;

    protected CartesianTuples(Tuple base, final Factor... factors) {
      super(new Domains<String, Object>() {
        @Override public List<String> getDomainNames() {
          List<String> ret = new ArrayList<String>(factors.length);
          for (Factor f : factors) {
            ret.add(f.name);
          }
          return ret;
        }

        @Override public List<Object> getDomain(String s) {
          Utils.checknotnull(s);
          for (Factor f : factors) {
            if (s.equals(f.name)) return f.levels;
          }
          return null;
        }
      });
      this.base = Utils.checknotnull(base);
    }

    @Override protected Tuple createMap() {
      return base.cloneTuple();
    }
  }

  public static CartesianTuples enumerateCartesianProduct(final Tuple base, Factor... factors) {
    Utils.checknotnull(base);
    return new CartesianTuples(base, factors);
  }

  public static Set<Tuple> subtuplesOf(
      Tuple tuple, int strength) {
    Utils.checknotnull(tuple);
    Utils.checkcond(strength >= 0 && strength <= tuple.size());
    Set<Tuple> ret = new HashSet<Tuple>();
    Combinator<String> c = new Combinator<String>(
        new LinkedList<String>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      Tuple cur = new TupleImpl();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  public static Set<Tuple> subtuplesOf(Tuple tuple) {
    Utils.checknotnull(tuple);
    Set<Tuple> ret = new HashSet<Tuple>();
    int sz = tuple.size();
    for (int i = 0; i <= sz; i++) {
      ret.addAll(subtuplesOf(tuple, sz - i));
    }
    return ret;
  }

  /**
   * Returns {@code true} if {@code t} is a sub-tuple of {@code u}, {@code false} otherwise.
   */
  public static boolean isSubtupleOf(Tuple t, Tuple u) {
    Utils.checknotnull(t);
    Utils.checknotnull(u);
    return t.isSubtupleOf(u);
  }

}
