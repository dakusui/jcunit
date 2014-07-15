package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.jcunit.core.Utils;

import java.util.*;

public class TupleUtils {
  public static boolean eq(Object v, Object o) {
    if (v == null) {
      return o == null;
    }
    return v.equals(o);
  }

  public static List<AttrValue<String, Object>> map2list(
      Map<String, Object[]> domains) {
    List<AttrValue<String, Object>> ret = new LinkedList<AttrValue<String, Object>>();
    for (String k : domains.keySet()) {
      for (Object v : domains.get(k)) {
        ret.add(new AttrValue<String, Object>(k, v));
      }
    }
    return ret;
  }

  public static Tuple list2tuple(
      List<AttrValue<String, Object>> attrValues) {
    Tuple ret = new TupleImpl();
    for (AttrValue<String, Object> cur : attrValues) {
      ret.put(cur.attr(), cur.value());
    }
    return ret;
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
}
