package com.github.dakusui.jcunit.constraints;

import com.sun.tools.internal.jxc.ap.Const;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.SortedSet;

/**
 * Created by hiroshi on 6/30/14.
 */
public abstract class ConstraintManagerBase<T, U> implements  ConstraintManager<T, U> {
  private final Set<ConstraintObserver<T, U>> observers = new LinkedHashSet<ConstraintObserver<T, U>>();

  @Override
  public void addObserver(ConstraintObserver<T, U> observer) {
    this.observers.add(observer);
  }

  @Override
  public void removeObservers(ConstraintObserver<T, U> observer) {
    this.observers.remove(observer);
  }

  public Set<ConstraintObserver<T, U>> observers() {
    return Collections.unmodifiableSet(observers());
  }
}
