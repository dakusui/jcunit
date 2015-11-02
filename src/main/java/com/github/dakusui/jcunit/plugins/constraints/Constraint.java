package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.plugins.Plugin;

import java.util.List;
import java.util.Set;

public interface Constraint extends Plugin {
  Constraint DEFAULT_CONSTRAINT_MANAGER = new NullConstraint();

  /**
   * Returns {@code true} if the given tuple doesn't violate any known constraints.
   * In case tuple doesn't have sufficient attribute values to be evaluated,
   * an {@code UndefinedSymbol} will be thrown.
   * Otherwise, {@code false} should be returned.
   *
   * @param tuple A tuple to be evaluated.
   * @return {@code true} - The tuple doesn't violate any constraints managed by this object / {@code false} - The tuple DOES violate a constraint.
   * @throws com.github.dakusui.jcunit.exceptions.UndefinedSymbol Failed to evaluate the tuple for insufficient attribute(s).
   */
  boolean check(Tuple tuple) throws UndefinedSymbol;

  Factors getFactors();

  void setFactors(Factors factors);

  void addObserver(Observer observer);

  Set<Observer> observers();

  void removeObservers(Observer observer);

  List<Tuple> getViolations();

  class Builder {
    private Class<? extends Constraint> constraintManagerClass;
    private Factors                     factors;

    public Constraint build() {
      return ReflectionUtils.create(constraintManagerClass);
    }

    public Builder setConstraintManagerClass(
        Class<? extends Constraint> constraintManagerClass) {
      this.constraintManagerClass = constraintManagerClass;
      return this;
    }

    public Builder setFactors(Factors factors) {
      this.factors = factors;
      return this;
    }

    public Factors getFactors() {
      return factors;
    }
  }

  interface Observer {
  }
}
