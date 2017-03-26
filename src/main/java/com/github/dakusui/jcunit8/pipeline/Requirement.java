package com.github.dakusui.jcunit8.pipeline;

public interface Requirement {
  int strength();
  boolean generateNegativeTests();
}
