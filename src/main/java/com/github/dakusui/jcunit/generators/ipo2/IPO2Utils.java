package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.jcunit.core.Utils;

import java.util.*;

/**
 * Created by hiroshi on 6/30/14.
 */
public class IPO2Utils {
  public static <T> T[] cloneArray(T[] arr) {
    Utils.checknotnull(arr);
    return Arrays.copyOf(arr, arr.length);
  }

  public static String nthKey(int index,
      LinkedHashMap<String, Object[]> domains) {
    Utils.checknotnull(domains);
    Utils.checkcond(index >= 0 && index < domains.size());
    Iterator<String> k = domains.keySet().iterator();
    for (int i = 0; i < domains.size(); i++) {
      String ret = k.next();
      if (i == index) {
        return ret;
      }
    }
    throw new RuntimeException();
  }

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
    Tuple ret = new Tuple();
    for (AttrValue<String, Object> cur : attrValues) {
      ret.put(cur.attr(), cur.value());
    }
    return ret;
  }
}
