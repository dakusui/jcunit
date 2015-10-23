package com.github.dakusui.jcunit.plugins.constraintmanagers;

import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.plugins.Plugin;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public abstract class ConstraintManagerBase<S> extends Plugin.Base<S> implements ConstraintManager {
  private final Set<Observer> observers = new LinkedHashSet<Observer>();
  private Factors factors;

  public ConstraintManagerBase(Param.Translator<S> translator) {
    super(translator);
  }

  public ConstraintManagerBase() {
    //noinspection unchecked
    this((Param.Translator<S>) Param.Translator.NULLTRANSLATOR);
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
}
