package com.github.dakusui.jcunit.core;

import org.junit.rules.TestName;

public class TestCaseDescription<V extends TestCaseDescription.ViolationCategory> extends TestName{
  public interface ViolationCategory {}
  public enum Eg implements ViolationCategory {}
  public V getViolationCategory() {
    return null;
  }
}
