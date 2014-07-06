package com.github.dakusui.jcunit.constraints;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Created by hiroshi on 6/30/14.
 */
public abstract class ConstraintManagerBase implements ConstraintManager {
  private final Set<ConstraintObserver> observers = new LinkedHashSet<ConstraintObserver>();

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
}
