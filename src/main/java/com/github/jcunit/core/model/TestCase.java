package com.github.jcunit.core.model;


public interface TestCase<P, B> {
  P given();

  B when(P prerequisites);

  void then(B behavior);
}
