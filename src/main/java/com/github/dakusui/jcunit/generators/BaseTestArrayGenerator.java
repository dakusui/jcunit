package com.github.dakusui.jcunit.generators;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.jcunit.core.GeneratorParameters;
import com.github.dakusui.jcunit.core.GeneratorParameters.Value;

public abstract class BaseTestArrayGenerator<T, U> implements
    TestArrayGenerator<T, U> {
  /**
   * A logger object.
   */
  private static final Logger     LOGGER  = LoggerFactory
                                              .getLogger(BaseTestArrayGenerator.class);

  protected LinkedHashMap<T, U[]> domains = null;
  protected long                  size    = -1;
  protected long                  cur     = -1;

  protected Value[]               params;

  @Override
  public void remove() {
    throw new UnsupportedOperationException();
  }

  @Override
  public boolean hasNext() {
    if (size < 0 || this.cur < 0)
      throw new IllegalStateException();
    return cur < size;
  }

  @Override
  public Iterator<Map<T, U>> iterator() {
    return this;
  }

  @Override
  public Map<T, U> next() {
    if (cur >= size)
      throw new NoSuchElementException();
    Map<T, U> ret = get(cur);
    cur++;
    return ret;
  }

  @Override
  public void init(GeneratorParameters.Value[] params,
      LinkedHashMap<T, U[]> domains) {
    // //
    // TODO: We shouldn't need to create another linked hash map anymore.
    this.domains = new LinkedHashMap<T, U[]>();
    this.domains.putAll(domains);
    List<T> ignoredKeys = new LinkedList<T>();
    for (T f : this.domains.keySet()) {
      U[] d = this.domains.get(f);
      if (d.length == 0) {
        ignoredKeys.add(f);
        LOGGER.warn(
            "The domain of '{}' is empty. This parameter will be ignored.", f);
        continue;
      }
    }
    for (T f : ignoredKeys) {
      this.domains.remove(f);
    }

    this.params = params;
    this.size = -1;
    this.cur = -1;
  }

  @Override
  public Map<T, U> get(long cur) {
    Map<T, U> ret = new LinkedHashMap<T, U>();
    for (T f : this.domains.keySet()) {
      U[] values = domains.get(f);
      ret.put(f, values[getIndex(f, cur)]);
    }
    return ret;
  }

  @Override
  public U[] getDomain(T key) {
    return this.domains.get(key);
  }

  public List<T> getKeys() {
    List<T> ret = new ArrayList<T>(this.domains.size());
    for (T k : this.domains.keySet()) {
      ret.add(k);
    }
    return ret;
  }

  @Override
  public long size() {
    if (this.size < 0)
      throw new IllegalStateException();
    return this.size;
  }
}
