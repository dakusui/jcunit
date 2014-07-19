package com.github.dakusui.jcunit.framework.utils.tuples;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.Set;

public class VerificationResult {
  private final Set<Tuple> invalidTuples;
  private final String     reason;

  public VerificationResult(String reason, Set<Tuple> invalidTuples) {
    this.invalidTuples = Collections.unmodifiableSet(Utils.checknotnull(invalidTuples));
    this.reason = Utils.checknotnull(reason);
  }

  public boolean isSuccessful(){
    return this.invalidTuples.isEmpty();
  }

  public void check() {
    if (this.invalidTuples.isEmpty()) {
      return;
    }
    throw new JCUnitAssertionError(composeErrorReport());
  }

  private String composeErrorReport() {
    return String.format("%s:%s", this.reason, invalidTuples);
  }
}
