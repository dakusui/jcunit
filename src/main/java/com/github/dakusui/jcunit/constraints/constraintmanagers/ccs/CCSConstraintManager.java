package com.github.dakusui.jcunit.constraints.constraintmanagers.ccs;

import com.github.dakusui.jcunit.constraints.ConstraintManagerBase;
import com.github.dakusui.jcunit.constraints.ConstraintRule;
import com.github.dakusui.jcunit.core.Tuple;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;
import com.github.dakusui.jcunit.generators.ipo2.IPO2Utils;
import com.github.dakusui.lisj.CUT;

import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/**
 */
public abstract class CCSConstraintManager extends ConstraintManagerBase {
  private Set<PotentialConstraint> potentialConstraints;

  public CCSConstraintManager() {
    this.potentialConstraints = new LinkedHashSet<PotentialConstraint>();
  }

  /**
   * Returns {@code null} if the given tuple doesn't violate constraint rules
   * explicitly.
   * If this method can't determine if the tuple doesn't violate them because
   * it lacks values of necessary attributes, it returns {@code null}, too.
   * <p/>
   * If and only if this method finds that the given tuple violates known constraints,
   * it returns a tuple which has attributes and their values involved in the
   * evaluation.
   *
   * @param tuple The tuple to be evaluated.
   * @return A tuple which has involved values in {@code tuple}.
   */
  Tuple checkTupleWithRules(Tuple tuple) {
    List<ConstraintRule> constraintRules = getConstraintRules();
    for (ConstraintRule c : constraintRules) {
      Tuple ret;
      try {
        ret = c.evaluate(tuple);
        if (ret != null) return ret;
      } catch (JCUnitCheckedException e) {
        e.printStackTrace();
      } catch (CUT cut) {
        cut.printStackTrace();
      }
    }
    return null;
  }

  @Override
  public boolean check(Tuple tuple) {
    Tuple violating;
    if ((violating = this.checkTupleWithRules(tuple)) == null) {
      return true;
    }
    Factors factors = this.getFactors();
    Set<Tuple> subtuplesOfViolatingTuple = IPO2Utils
        .subtuplesOf(violating, violating.size() - 1);
    for (Tuple t : subtuplesOfViolatingTuple) {
      String removedFactorName = findMissingFactorName(t, violating);
      Set<PotentialConstraint> pcSet = findPotentialConstraintsFor(t,
          removedFactorName);
      for (PotentialConstraint pc : pcSet) {
        if (pc == null) {
          pc = new PotentialConstraint(t, factors.get(removedFactorName));
          registerPotentialConstraint(pc);
        }
        Tuple implicitConstraint = pc.removeLevel(t.get(removedFactorName));
        if (implicitConstraint != null) {
          ////
          // Now an implicit constraint is found.
          // Remove all super tuples.
          // Notify observers.
          this.unregisterImplicitConstraint(implicitConstraint);
          this.implicitConstraintFound(implicitConstraint);
        }
      }
    }
    return false;
  }

  private void unregisterImplicitConstraint(Tuple implicitConstraint) {
    Set<PotentialConstraint> remove = new HashSet<PotentialConstraint>();
    for (PotentialConstraint k : this.potentialConstraints) {
      if (implicitConstraint
          .isSubtupleOf(k)) {
        remove.add(k);
      }
    }
    this.potentialConstraints.removeAll(remove);
  }

  private void registerPotentialConstraint(PotentialConstraint pc) {
    this.potentialConstraints.add(pc);
  }

  Set<PotentialConstraint> findPotentialConstraintsFor(Tuple t,
      String remainder) {
    Utils.checknotnull(t);
    Utils.checknotnull(remainder);
    Set<PotentialConstraint> ret = new LinkedHashSet<PotentialConstraint>();
    for (PotentialConstraint pc : this.potentialConstraints) {
      if (t.isSubtupleOf(pc) && remainder.equals(pc.getRemainderFactorName())) {
        ret.add(pc);
      }
    }
    return ret;
  }

  private String findMissingFactorName(Tuple subtuple, Tuple tuple) {
    Utils.checknotnull(subtuple);
    Utils.checknotnull(tuple);
    Utils.checkcond(tuple.size() - subtuple.size() == 1);
    Utils.checkcond(tuple.keySet().containsAll(subtuple.keySet()));
    for (String k : tuple.keySet()) {
      if (!subtuple.containsKey(k)) {
        return k;
      }
    }
    Utils.checkcond(false, "Something went wrong.");
    return null; // This line will never be executed.
  }

  public abstract List<ConstraintRule> getConstraintRules();
}
