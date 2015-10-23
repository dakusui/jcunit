package com.github.dakusui.jcunit.plugins.constraintmanagers;

import com.github.dakusui.jcunit.plugins.JCUnitPlugin;
import com.github.dakusui.jcunit.standardrunner.annotations.Arg;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ConstraintManagerBase extends JCUnitPlugin.Base implements ConstraintManager {
  private final Set<Observer> observers = new LinkedHashSet<Observer>();
  private Factors factors;

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
  public Arg.Type[] parameterTypes() {
    return new Arg.Type[0];
  }
}
