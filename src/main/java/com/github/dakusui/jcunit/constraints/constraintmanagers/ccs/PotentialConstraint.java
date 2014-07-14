package com.github.dakusui.jcunit.constraints.constraintmanagers.ccs;

import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.UnmodifiableTuple;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;

import java.util.HashSet;
import java.util.Set;

public class PotentialConstraint extends UnmodifiableTuple {
  private final String      remainderFactorName;
  private final Set<Object> remainingLevels;

  public PotentialConstraint(Tuple tuple, Factor remainder) {
    super(tuple);
    Utils.checknotnull(tuple);
    Utils.checknotnull(remainder);
    this.remainderFactorName = remainder.name;
    this.remainingLevels = new HashSet<Object>();
    this.remainingLevels.addAll(remainingLevels);
  }

  public String getRemainderFactorName() {
    return this.remainderFactorName;
  }

  /**
   * Returns {@code null} if there is still remaining levels to be covered.
   *
   * @param v A level in a factor identified by {@code remainingFactorName}.
   * @return
   */
  public Tuple removeLevel(Object v) {
    remainingLevels.remove(v);
    if (remainingLevels.isEmpty()) {
      return Utils.unmodifiableTuple(this);
    }
    return null;
  }

  @Override public boolean isSubtupleOf(Tuple another) {
    throw new UnsupportedOperationException();
  }

  @Override
  public int hashCode() {
    return this.remainderFactorName.hashCode() + super.hashCode();
  }

  @Override
  public boolean equals(Object e) {
    return super.equals(e) && this.remainderFactorName
        .equals(((PotentialConstraint) e).remainderFactorName);
  }

}
