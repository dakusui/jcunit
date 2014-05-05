package com.github.dakusui.jcunit.constraints.ccs;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;

public class ConstraintSet<T, U> {
  Map<AttrValue<T, U>, List<Constraint<T, U>>> invertedIndexForConstraint = new HashMap<AttrValue<T, U>, List<Constraint<T, U>>>();
  Map<T, Set<AttrValue<T, U>>> remainingValues;
  private final Map<T, List<U>> domains;

  /**
   * @param domains
   *          A set of domains whose values list all the possible values for the
   *          parameter represented by keys.
   */
  public ConstraintSet(Map<T, List<U>> domains) {
    this.domains = domains;
    for (T attr : domains.keySet()) {
      List<U> currentDomain = domains.get(attr);
      Set<AttrValue<T, U>> attrValues = new HashSet<AttrValue<T, U>>();
      this.remainingValues.put(attr, attrValues);
      for (U value : currentDomain) {
        attrValues.add(new AttrValue<T, U>(attr, value));
      }
    }
  }

  public void add(Constraint<T, U> constraint) {
    for (T attr : constraint.keySet()) {
      registerConstraintToAttr(constraint, attr);
    }
  }

  /**
   * Registers given constraint to the inverted index.
   * 
   * @param constraint
   *          A constraint to be indexed.
   * @param attr
   *          An attribute on which the constraint is indexed.
   */
  protected void registerConstraintToAttr(Constraint<T, U> constraint, T attr) {
    U value = constraint.get(attr);
    AttrValue<T, U> attrValue = new AttrValue<T, U>(attr, value);
    List<Constraint<T, U>> constraintsForAttrValue = invertedIndexForConstraint.get(attrValue);
    if (constraintsForAttrValue == null) {
      constraintsForAttrValue = new LinkedList<Constraint<T, U>>();
      this.invertedIndexForConstraint.put(attrValue, constraintsForAttrValue);
    }
    constraintsForAttrValue.add(constraint);
    boolean removed = this.remainingValues.get(attr).remove(attrValue);
    if (this.remainingValues.get(attr).isEmpty()) {
      constraintsForAttrValue.add(constraint);
      newCoveringConstraint(constraint, attr);
      if (removed) {
      }
    }
  }

  void newCoveringConstraint(Constraint<T, U> constraint, T attr) {
    // //
    // Identify value for attr in the newly found constraint.
    U valueCoveredByConstraint = constraint.get(attr);
    // //
    // Create a list of pairs whose keys are 'attribute values' and values are
    // corresponding constraints.
    // But constraints for the attribute value covered by the newly covered
    // constraint is excluded.
    List<AttrValue<AttrValue<T, U>, Constraint<T, U>>> attrValues = new LinkedList<AttrValue<AttrValue<T, U>, Constraint<T, U>>>();
    for (U value : this.domains.get(attr)) {
      if (!eq(value, valueCoveredByConstraint)) {
        for (Constraint<T, U> cur : this.invertedIndexForConstraint.get(attr)) {
          attrValues.add(new AttrValue<AttrValue<T, U>, Constraint<T, U>>(new AttrValue<T, U>(attr, value), cur));
        }
      }
    }
    // //
    // Add a value pair for newly found constraint.
    attrValues.add(new AttrValue<AttrValue<T, U>, Constraint<T, U>>(
        new AttrValue<T, U>(attr, valueCoveredByConstraint), constraint));

    // //
    // Iterates over all possible sets that cover all the values of attribute
    // 'attr'.
    CartesianEnumerator<AttrValue<T, U>, Constraint<T, U>> enumerator = new CartesianEnumerator<AttrValue<T, U>, Constraint<T, U>>(
        attrValues);
    for (List<AttrValue<AttrValue<T, U>, Constraint<T, U>>> v : enumerator) {
      Constraint<T, U> next = new Constraint<T, U>();
      for (AttrValue<AttrValue<T, U>, Constraint<T, U>> w : v) {
        Constraint<T, U> cur = new Constraint<T, U>();
        cur.putAll(w.value());
        cur.remove(attr);
        if ((next = cur.merge(next)) == null)
          break;
      }
      if (next != null) {
        // //
        // If next is not null, then it is an implied constraint.
      }
    }

  }

  void newlyCoveredParameterFound(T t) {

  }

  Iterable<U> allPossibleValuesOf(T attr) {
    return this.domains.get(attr);
  }

  private boolean eq(U a, U b) {
    if (a == null)
      return b == null;
    return a.equals(b);
  }
}
