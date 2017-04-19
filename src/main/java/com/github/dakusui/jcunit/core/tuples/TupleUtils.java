package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.combinatoradix.Combinator;
import com.github.dakusui.jcunit.core.utils.Checks;

import java.lang.reflect.Array;
import java.util.*;

public enum TupleUtils {
  ;

  public static Set<Tuple> subtuplesOf(
      Tuple tuple, int strength) {
    Checks.checknotnull(tuple);
    Checks.checkcond(strength >= 0 && strength <= tuple.size());
    Set<Tuple> ret = new LinkedHashSet<>();
    Combinator<String> c = new Combinator<>(
        new LinkedList<>(tuple.keySet()), strength);
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
    Set<Tuple> ret = new LinkedHashSet<>();
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

}
