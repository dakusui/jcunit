package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.CartesianEnumeratorAdaptor;
import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.combinatoradix.Domains;
import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.utils.IOUtils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.exceptions.SavedObjectBrokenException;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.*;

public enum TupleUtils {
  ;

  public static CartesianTuples enumerateCartesianProduct(final Tuple base, Factor... factors) {
    Checks.checknotnull(base);
    return new CartesianTuples(base, factors);
  }

  public static Set<Tuple> subtuplesOf(
      Tuple tuple, int strength) {
    Checks.checknotnull(tuple);
    Checks.checkcond(strength >= 0 && strength <= tuple.size());
    Set<Tuple> ret = new LinkedHashSet<Tuple>();
    Combinator<String> c = new Combinator<String>(
        new LinkedList<String>(tuple.keySet()), strength);
    for (List<String> keys : c) {
      Tuple cur = new Tuple.Impl();
      for (String k : keys) {
        cur.put(k, tuple.get(k));
      }
      ret.add(cur);
    }
    return ret;
  }

  public static Set<Tuple> subtuplesOf(Tuple tuple) {
    Checks.checknotnull(tuple);
    Set<Tuple> ret = new LinkedHashSet<Tuple>();
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
    Checks.checknotnull(t);
    Checks.checknotnull(u);
    return t.isSubtupleOf(u);
  }

  public static Tuple unmodifiableTuple(Tuple tuple) {
    Checks.checknotnull(tuple);
    return new Tuple.Builder().putAll(tuple).setUnmodifiable(true).build();
  }

  public static String toString(Collection<Tuple> tuples) {
    StringBuilder b = new StringBuilder();
    b.append('[');
    boolean firstTime = true;
    for (Tuple t : tuples) {
      if (!firstTime)
        b.append(",");
      b.append(toString(t));
      firstTime = false;
    }
    b.append(']');
    return b.toString();
  }

  public static String toString(Tuple tuple) {
    Checks.checknotnull(tuple);
    return tupleToString(tuple);
  }

  private static String escape(Object v) {
    String ret = v.toString();
    ret = ret.replaceAll("\\\\", "\\\\\\\\");
    ret = ret.replaceAll("\"", "\\\\\"");
    return ret;
  }

  private static String arrToString(Object v) {
    int len = Array.getLength(v);
    StringBuilder b = new StringBuilder();
    b.append('[');
    for (int i = 0; i < len; i++) {
      if (i > 0) {
        b.append(',');
      }
      b.append(valueToString(Array.get(v, i)));
    }
    b.append(']');
    return b.toString();
  }

  private static String tupleToString(Tuple tuple) {
    StringBuilder b = new StringBuilder();
    Set<String> keySet = tuple.keySet();
    b.append('{');
    boolean firstTime = true;
    for (String k : keySet) {
      if (!firstTime) {
        b.append(',');
      }
      Object v = tuple.get(k);
      b.append(String.format("\"%s\":%s", escape(k),
          valueToString(v)
      ));
      firstTime = false;
    }
    b.append('}');
    return b.toString();
  }

  private static String valueToString(Object v) {
    return v == null ? null
        : v instanceof Tuple ? tupleToString((Tuple) v)
        : v instanceof Number ? v.toString()
        : v.getClass().isArray() ? arrToString(v)
        : String.format("\"%s\"", escape(v));
  }

  public static void save(Tuple tuple, OutputStream os) {
    IOUtils.save(tuple, os);
  }

  public static Tuple load(InputStream is) {
    Object obj;
    try {
      obj = IOUtils.load(is);
    } catch (JCUnitException e) {
      throw new SavedObjectBrokenException("Saved object was broken.", e);
    }
    if (obj instanceof Tuple) {
      return (Tuple) obj;
    }
    throw new SavedObjectBrokenException(String.format("Saved object wasn't a tuple (%s)", obj.getClass().getCanonicalName()), null);
  }

  public static class CartesianTuples extends CartesianEnumeratorAdaptor<Tuple, String, Object> {

    private final Tuple base;

    protected CartesianTuples(Tuple base, final Factor... factors) {
      super(new Domains<String, Object>() {
        @Override
        public List<String> getDomainNames() {
          List<String> ret = new ArrayList<String>(factors.length);
          for (Factor f : factors) {
            ret.add(f.name);
          }
          return ret;
        }

        @Override
        public List<Object> getDomain(String s) {
          Checks.checknotnull(s);
          for (Factor f : factors) {
            if (s.equals(f.name)) {
              return f.levels;
            }
          }
          return null;
        }
      });
      this.base = Checks.checknotnull(base);
    }

    @Override
    protected Tuple createMap() {
      return base.cloneTuple();
    }
  }
}
