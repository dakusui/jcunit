package com.github.dakusui.jcunit8.core;

public interface Config {
  Requirement getRequirement();
  enum Feature {
    NEGATIVE_TESTS
  }
}
