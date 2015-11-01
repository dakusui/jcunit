package com.github.dakusui.jcunit.runners.core;

import com.github.dakusui.jcunit.core.Utils;

import java.util.AbstractList;
import java.util.List;

public class TestSuite extends AbstractList<TestCase> {

  private final List<TestCase> testCases;

  public TestSuite(List<TestCase> testCases) {
    this.testCases = Utils.newUnmodifiableList(testCases);
  }

  @Override
  public TestCase get(int index) {
    return this.testCases.get(index);
  }

  @Override
  public int size() {
    return this.testCases.size();
  }
}
