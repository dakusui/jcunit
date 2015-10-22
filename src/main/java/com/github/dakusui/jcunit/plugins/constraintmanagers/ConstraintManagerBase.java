package com.github.dakusui.jcunit.plugins.constraintmanagers;

import com.github.dakusui.jcunit.standardrunner.annotations.Param;
import com.github.dakusui.jcunit.plugins.JCUnitConfigurablePluginBase;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ConstraintManagerBase extends JCUnitConfigurablePluginBase implements ConstraintManager {
  private final Set<Observer> observers = new LinkedHashSet<Observer>();
  private Factors factors;

  @Override
  protected void init(Object[] params) {
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
  public void addObserver(Observer observer) {
    this.observers.add(observer);
  }

  @Override
  public void removeObservers(Observer observer) {
    this.observers.remove(observer);
  }

  public Set<Observer> observers() {
    return Collections.unmodifiableSet(observers);
  }

  @Override
  public List<Tuple> getViolations() {
    return Collections.emptyList();
  }

  @Override
  public Param.Type[] parameterTypes() {
    return new Param.Type[0];
  }
}
