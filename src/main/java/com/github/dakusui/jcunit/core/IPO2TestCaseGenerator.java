package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.generators.TestCaseGenerator;

import java.util.Iterator;
import java.util.List;

public class IPO2TestCaseGenerator implements TestCaseGenerator {
  @Override public Factor getFactor(String factorName) {
    return null;
  }

  @Override public void init(String[] params,
      Factors factors) {

  }

  @Override public int getIndex(String factorName, long testId) {
    return 0;
  }

  @Override public List<String> getFactorNames() {
    return null;
  }

  @Override public Tuple get(long testId) {
    return null;
  }

  @Override public long size() {
    return 0;
  }

  @Override public Iterator<Tuple> iterator() {
    return null;
  }

  @Override public boolean hasNext() {
    return false;
  }

  @Override public Tuple next() {
    return null;
  }

  @Override public void remove() {

  }
}
