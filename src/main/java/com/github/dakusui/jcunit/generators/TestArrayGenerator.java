package com.github.dakusui.jcunit.generators;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import com.github.dakusui.jcunit.core.GeneratorParameters;

public interface TestArrayGenerator<T, U> extends Iterator<Map<T, U>>,
    Iterable<Map<T, U>> {

  /*
   * <code>null</code> will be returned if undefined key is specified.
   */
  public U[] getDomain(T key);

  public void init(GeneratorParameters.Value[] params,
      LinkedHashMap<T, U[]> domains);

  /**
   * Returns an index for <code>cur</code>
   * 
   * @param key
   * @param cur
   * @return
   */
  int getIndex(T key, long cur);

  List<T> getKeys();

  public Map<T, U> get(long cur);

  long size();
}
