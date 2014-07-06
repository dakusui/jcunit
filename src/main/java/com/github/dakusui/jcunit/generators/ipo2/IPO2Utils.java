package com.github.dakusui.jcunit.generators.ipo2;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.ValueTuple;

import java.util.*;
import java.util.function.BiConsumer;

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

  public static <K, V> LinkedHashMap<K, V> headMap(LinkedHashMap<K, V> map,
      final K to) {
    Utils.checknotnull(map);
    Utils.checkcond(map.containsKey(to));
    // Since it is already made sure that the map contains 'to' as key, it is
    // safe to assume at least one key.
    return extractSubMap(map, map.keySet().iterator().next(), to, false);
  }

  public static <K, V> K nextKey(LinkedHashMap<K, V> map, final K to) {
    Utils.checknotnull(map);
    Iterator<K> i = map.keySet().iterator();
    while (i.hasNext()) {
      if (IPO2Utils.eq(i.next(), to) && i.hasNext()) {
        return i.next();
      }
    }
    Utils.checkcond(false, String.format("The given key '%s' wasn't found in the map's keys or the last one:%s ", to, map));
    return null; // This line will never be executed.
  }

  public static <K, V> boolean isLastKey(LinkedHashMap<K, V> map, final K k) {
    Utils.checknotnull(map);
    Utils.checkcond(map.size() > 0);
    Utils.checkcond(map.containsKey(k));
    Iterator<K> i = map.keySet().iterator();
    while (i.hasNext()) {
      K cur = i.next();
      if (i.hasNext()) {
        if (IPO2Utils.eq(cur, k))
          return false;
      } else {
        if (IPO2Utils.eq(cur, k))
          return true;
      }
    }
    throw new RuntimeException("Something went wrong");
  }

  public static <K, V> LinkedHashMap<K, V> tailMap(LinkedHashMap<K, V> map,
      final K from) {
    Utils.checknotnull(map);
    Utils.checkcond(map.containsKey(from));
    return extractSubMap(map, from, null, true);
  }

  public static <K, V> LinkedHashMap<K, V> subMap(LinkedHashMap<K, V> map,
      final K from, final K to) {
    Utils.checknotnull(map);
    Utils.checkcond(map.containsKey(from));
    Utils.checkcond(map.containsKey(to));
    return extractSubMap(map, from, to, false);
  }

  private static <K, V> LinkedHashMap<K, V> extractSubMap(
      LinkedHashMap<K, V> map, final K from, final K to,
      final boolean ignoreTo) {
    final LinkedHashMap<K, V> ret = new LinkedHashMap<K, V>();
    map.forEach(new BiConsumer<K, V>() {
      boolean fromFound = false;
      boolean toFound = false;

      @Override public void accept(K k, V v) {
        if (k.equals(from)) {
          fromFound = true;
        }
        if (!ignoreTo && k.equals(to)) {
          toFound = true;
        }
        Utils.checkcond(!(toFound && !fromFound), String.format(
            "Something wrong: (from, to, key, ignoreTo)=(%s, %s, %s, %s)", from,
            to, k, ignoreTo));
        if (fromFound && !toFound) {
          ret.put(k, v);
        }
      }
    });
    return ret;
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

  public static ValueTuple<String, Object> list2tuple(
      List<AttrValue<String, Object>> attrValues) {
    ValueTuple<String, Object> ret = new ValueTuple<String, Object>();
    for (AttrValue<String, Object> cur : attrValues) {
      ret.put(cur.attr(), cur.value());
    }
    return ret;
  }
}
