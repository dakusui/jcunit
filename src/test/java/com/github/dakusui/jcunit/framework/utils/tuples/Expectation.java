package com.github.dakusui.jcunit.framework.utils.tuples;

import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.List;

public interface Expectation {
  public VerificationResult verify(List<Tuple> tuples);
}
