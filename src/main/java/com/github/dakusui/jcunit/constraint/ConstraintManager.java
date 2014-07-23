package com.github.dakusui.jcunit.constraint;

import com.github.dakusui.jcunit.constraint.constraintmanagers.NullConstraintManager;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.JCUnitSymbolException;

import java.util.List;

public interface ConstraintManager {
  public static final ConstraintManager DEFAULT_CONSTRAINT_MANAGER = new NullConstraintManager();

  void init(Object[] params);

  /**
   * Returns {@code true} if the given tuple satisfies all the known constraints.
   * In case tuple doesn't have sufficient attribute values to be evaluated,
   * a {@code SymbolNotFoundException} will be thrown.
   * Otherwise, {@code false} will be returned.
   *
   * @param tuple A tuple to be evaluated.
   * @return {@code true} - The tuple doesn't violate constraints / {@code false} - otherwise.
   * @throws com.github.dakusui.jcunit.exceptions.JCUnitSymbolException Failed to evaluate the tuple for insufficient attribute(s).
   */
  boolean check(Tuple tuple) throws JCUnitSymbolException;

  Factors getFactors();

  void setFactors(Factors factors);

  void addObserver(ConstraintObserver observer);

  void removeObservers(ConstraintObserver observer);

  List<LabeledTestCase> getViolations();

}
