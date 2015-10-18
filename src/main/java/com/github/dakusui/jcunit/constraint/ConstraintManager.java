package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.constraint.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.JCUnitConfigurablePlugin;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.List;
import java.util.Set;

public interface ConstraintManager extends JCUnitConfigurablePlugin {
  ConstraintManager DEFAULT_CONSTRAINT_MANAGER = new NullConstraintManager();

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

  void addObserver(ConstraintObserver observer);

  Set<ConstraintObserver> observers();

  void removeObservers(ConstraintObserver observer);

  List<Tuple> getViolations();

  class Builder {
    private Class<? extends ConstraintManager> constraintManagerClass;
    private Param[]                            parameters;
    private Factors                            factors;

    public ConstraintManager build() {
      ConstraintManager ret = Utils
          .createNewInstanceUsingNoParameterConstructor(constraintManagerClass);
      ret.init(this.parameters);
      return ret;
    }

    public Builder setParameters(Param[] parameters) {
      this.parameters = parameters;
      return this;
    }

    public Builder setConstraintManagerClass(
        Class<? extends ConstraintManager> constraintManagerClass) {
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
}
