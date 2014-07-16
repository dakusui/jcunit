package com.github.dakusui.jcunit.constraint.constraintmanagers;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.ConstraintObserver;
import com.github.dakusui.jcunit.constraint.Violation;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.factor.Factors;

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

	protected void implicitConstraintFound(Tuple tuple) {
		for (ConstraintObserver o : this.observers) {
			o.implicitConstraintFound(tuple);
		}
	}

  @Override
  public List<Violation> getViolations() {
    return Collections.emptyList();
  }

  protected Violation createViolation(Object id, Tuple testCase){
    Utils.checknotnull(id);
    Utils.checknotnull(testCase);
    return new Violation.Builder().setId(id).setTestCase(testCase).build();
  }
}
