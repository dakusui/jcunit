package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.jcunit.core.Utils;

import java.util.*;

public class UnmodifiableTuple implements Tuple {
  private final Map<String, Object> map;

  public UnmodifiableTuple(Map<String, Object> map) {
    Utils.checknotnull(map);
    this.map = new TreeMap<String, Object>();
    this.map.putAll(map);
  }

  @Override
  public int size() {
    return map.size();
  }

  @Override
  public boolean isEmpty() {
    return map.isEmpty();
  }

  @Override
  public boolean containsKey(Object o) {
    return map.containsKey(o);
  }

  @Override
  public boolean containsValue(Object o) {
    return map.containsValue(o);
  }

  @Override
  public Object get(Object o) {
    return map.get(o);
  }

  @Override
  public Object put(String s, Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public Object remove(Object o) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void putAll(Map<? extends String, ?> map) {
    throw new UnsupportedOperationException();
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException();
  }

  @Override
  public Set<String> keySet() {
    return Collections.unmodifiableSet(map.keySet());
  }

  @Override
  public Collection<Object> values() {
    return Collections.unmodifiableCollection(map.values());
  }

  @Override
  public Set<Entry<String, Object>> entrySet() {
    return Collections.unmodifiableSet(map.entrySet());
  }

  @Override
  public Tuple cloneTuple() {
    return this;
  }

  @Override
  public boolean isSubtupleOf(Tuple another) {
    return TupleImpl.isSubtupleOf(this, another);
  }

  @Override
  public String toString() {
    return this.map.toString();
  }
}
