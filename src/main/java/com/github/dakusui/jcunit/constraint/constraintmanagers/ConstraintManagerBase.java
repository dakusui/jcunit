package com.github.dakusui.jcunit.constraint.constraintmanagers;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.ConstraintObserver;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ConstraintManagerBase implements ConstraintManager {
  private final Set<ConstraintObserver> observers = new LinkedHashSet<ConstraintObserver>();
  private Factors factors;

  @Override
  public void init(Object[] params) {
  }

  @Override
  public void setFactors(Factors factors) {
    this.factors = factors;
  }

  @Override
  public Factors getFactors() {
    return this.factors;
  }

  @Override
  public void addObserver(ConstraintObserver observer) {
    this.observers.add(observer);
  }

  @Override
  public void removeObservers(ConstraintObserver observer) {
    this.observers.remove(observer);
  }

  public Set<ConstraintObserver> observers() {
    return Collections.unmodifiableSet(observers());
  }

  /**
   * A utility method that sub classes can call when they find an implicit
   * constraints.
   *
   * @param tuple A tuple which represents an implicit constraint.
   */
  protected void implicitConstraintFound(Tuple tuple) {
    for (ConstraintObserver o : this.observers) {
      o.implicitConstraintFound(tuple);
    }
  }

  @Override
  public List<Tuple> getViolations() {
    return Collections.emptyList();
  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[0];
  }
}
