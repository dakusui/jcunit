package com.github.dakusui.jcunit8.sandbox.core;


public interface TestCase<P, B> {
  P given();

  B when(P prerequisites);

  void then(B behavior);
}
