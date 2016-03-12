package com.github.dakusui.jcunit.plugins.constraints;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;

import java.util.*;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public abstract class SmartConstraintCheckerBase implements ConstraintChecker {
  /**
   * Constraints to be covered by this checker.
   * When {@code check} method is called the first time, this will be
   * initialized
   */
  private       Set<Constraint> constraintsToBeCovered = null;
  private final List<Tuple>     chosenViolations;
  private final Set<Tuple>      factorLevelsToBeCovered;
  /**
   * This will be used as a 'template' to generate 'violation' test cases.
   */
  private Tuple regularTestCase = null;


  public SmartConstraintCheckerBase(Factors factors) {
    this.chosenViolations = new LinkedList<Tuple>();
    if (factors == null) {
      ////
      // factorSpace can be null on validation phase.
      this.factorLevelsToBeCovered = null;
    } else {
      this.factorLevelsToBeCovered = new LinkedHashSet<Tuple>(
          factors.generateAllPossibleTuples(1)
      );
    }
  }

  public boolean check(Tuple tuple) throws UndefinedSymbol {
    if (checkTupleAndUpdateViolations(tuple)) {
      if (constraintsToBeCovered == null) {
        this.constraintsToBeCovered = new LinkedHashSet<Constraint>(getConstraints());
      }
      if (regularTestCase == null) {
        regularTestCase = tuple;
      }
      if (!factorLevelsToBeCovered.isEmpty()) {
        this.factorLevelsToBeCovered.removeAll(TupleUtils.subtuplesOf(tuple, 1));
      }
      return true;
    }
    return false;
  }

  @Override
  public List<Tuple> getViolations() {
    List<Tuple> ret = new LinkedList<Tuple>(this.chosenViolations);
    for (Tuple factorLevel : this.factorLevelsToBeCovered) {
      ret.add(new Tuple.Builder()
          .putAll(this.regularTestCase)
          .putAll(factorLevel)
          .build());
    }
    return ret;
  }

  @Override
  public List<String> getTags() {
    return Utils.dedup(Utils.transform(this.getConstraints(),
        new Utils.Form<Constraint, String>() {
          @Override
          public String apply(Constraint in) {
            return in.tag();
          }
        }
    ));
  }

  @Override
  public boolean violates(final Tuple tuple, final String constraintTag) {
    checknotnull(constraintTag);
    checknotnull(tuple);
    return Utils.filter(
        Utils.filter(
            this.getConstraints(),
            new Utils.Predicate<Constraint>() {
              @Override
              public boolean apply(Constraint in) {
                return constraintTag.equals(in.tag());
              }
            }
        ),
        new Utils.Predicate<Constraint>() {
          @Override
          public boolean apply(Constraint in) {
            try {
              return !in.check(tuple);
            } catch (UndefinedSymbol undefinedSymbol) {
              // This shouldn't happen because JCUnit calls violates method only with
              // 'complete' tuple.
              throw Checks.wrap(undefinedSymbol);
            }
          }
        }).size() > 0;
  }

  protected abstract List<Constraint> getConstraints();


  private boolean checkTupleAndUpdateViolations(Tuple tuple) throws UndefinedSymbol {
    Set<Constraint> violatedConstraints = new HashSet<Constraint>();
    for (Constraint each : this.getConstraints()) {
      if (!each.check(tuple)) {
        violatedConstraints.add(each);
      }
    }
    if (violatedConstraints.size() == 1) {
      Constraint constraint = violatedConstraints.iterator().next();
      if (constraintsToBeCovered.contains(constraint)) {
        this.constraintsToBeCovered.remove(constraint);
        this.factorLevelsToBeCovered.removeAll(TupleUtils.subtuplesOf(tuple, 1));
        this.chosenViolations.add(tuple);
      }
    }
    return violatedConstraints.isEmpty();
  }

}
