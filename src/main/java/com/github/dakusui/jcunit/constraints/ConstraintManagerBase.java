package com.github.dakusui.jcunit.constraints;

import com.github.dakusui.jcunit.core.Tuple;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;

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

	protected void implicitConstraintFound(Tuple tuple) {
		for (ConstraintObserver o : this.observers) {
			o.implicitConstraintFound(tuple);
		}
	}
}
