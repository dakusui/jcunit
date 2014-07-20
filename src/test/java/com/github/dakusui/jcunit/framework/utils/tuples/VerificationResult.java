package com.github.dakusui.jcunit.framework.utils.tuples;

import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.Set;

public class VerificationResult {
  private final Set<Tuple> invalidTuples;
  private final String     reason;
  private final boolean    successful;

  public VerificationResult(String reason, Set<Tuple> invalidTuples) {
    this.invalidTuples = Collections
        .unmodifiableSet(Utils.checknotnull(invalidTuples));
    this.reason = Utils.checknotnull(reason);
    this.successful = this.invalidTuples.isEmpty();
  }

  public VerificationResult(String msg, boolean successful) {
    this.invalidTuples = null;
    this.reason = Utils.checknotnull(msg);
    this.successful = successful;
  }

  public VerificationResult(boolean successful) {
    this.invalidTuples = null;
    this.reason = null;
    this.successful = successful;
  }

  public boolean isSuccessful(){
    return this.successful;
  }

  public void check() {
    if (this.successful) {
      return;
    }
    throw new JCUnitAssertionError(composeErrorReport());
  }

  public String composeErrorReport() {
    return String.format("%s:%s",
        this.reason == null ? "(N/A)" : this.reason,
        invalidTuples == null ? "(N/A)" : invalidTuples);
  }
}
