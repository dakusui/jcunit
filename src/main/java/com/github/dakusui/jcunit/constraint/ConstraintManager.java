package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.constraint.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.JCUnitConfigurablePlugin;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.List;
import java.util.Set;

public interface ConstraintManager extends JCUnitConfigurablePlugin {
  public static final ConstraintManager DEFAULT_CONSTRAINT_MANAGER = new NullConstraintManager();

  /**
   * Returns {@code true} if the given tuple satisfies all the known constraints.
   * In case tuple doesn't have sufficient attribute values to be evaluated,
   * a {@code SymbolNotFoundException} will be thrown.
   * Otherwise, {@code false} will be returned.
   *
   * @param tuple A tuple to be evaluated.
   * @return {@code true} - The tuple doesn't violate constraints / {@code false} - otherwise.
   * @throws com.github.dakusui.jcunit.exceptions.UndefinedSymbol Failed to evaluate the tuple for insufficient attribute(s).
   */
  boolean check(Tuple tuple) throws UndefinedSymbol;

  Factors getFactors();

  void setFactors(Factors factors);

  void addObserver(ConstraintObserver observer);

  Set<ConstraintObserver> observers();

  void removeObservers(ConstraintObserver observer);

  List<Tuple> getViolations();

}
