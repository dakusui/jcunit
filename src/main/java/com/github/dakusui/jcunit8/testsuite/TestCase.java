package com.github.dakusui.jcunit8.testsuite;

import com.github.dakusui.jcunit.core.tuples.Tuple;

public interface TestCase {
  Description getDescription();
  Tuple getPayload();
  interface Description {
  }
}
