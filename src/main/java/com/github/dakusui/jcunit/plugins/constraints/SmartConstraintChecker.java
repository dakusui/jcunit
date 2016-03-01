package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.lang.reflect.ParameterizedType;
import java.util.*;

/**
 */
public abstract class SmartConstraintChecker<C extends Enum & Constraint> implements ConstraintChecker {

  private final Set<C>      constraintsToBeCovered;
  private final List<Tuple> chosenViolations;

  public SmartConstraintChecker() {
    this.constraintsToBeCovered = new HashSet<C>(Arrays.asList(this.getConstraintClass().getEnumConstants()));
    this.chosenViolations = new LinkedList<Tuple>();
  }

  @Override
  public boolean check(Tuple tuple) throws UndefinedSymbol {
    Set<C> violatedConstraints = new HashSet<C>();
    for (C each : this.getConstraintClass().getEnumConstants()) {
      if (!each.check(tuple)) {
        violatedConstraints.add(each);
      }
    }
    if (violatedConstraints.size() == 1) {
      C constraint = violatedConstraints.iterator().next();
      if (constraintsToBeCovered.contains(constraint)) {
        this.constraintsToBeCovered.remove(constraint);
        tuple.put("#violatedconstraint", constraint);
        this.chosenViolations.add(tuple);
      }
    }
    return violatedConstraints.isEmpty();
  }

  protected Class<C> getConstraintClass() {
    ParameterizedType superclass =
        (ParameterizedType) getClass().getGenericSuperclass();

    return (Class<C>) superclass.getActualTypeArguments()[0];
  }

  @Override
  public List<Tuple> getViolations() {
    return this.chosenViolations;
  }
}
