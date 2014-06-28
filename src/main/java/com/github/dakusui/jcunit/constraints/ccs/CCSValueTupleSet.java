package com.github.dakusui.jcunit.constraints.ccs;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.core.ValueTuple;

import java.util.*;

public class CCSValueTupleSet<T, U> {
  Map<AttrValue<T, U>, List<ValueTuple<T, U>>> invertedIndexForConstraint = new HashMap<AttrValue<T, U>, List<ValueTuple<T, U>>>();
  Map<T, Set<AttrValue<T, U>>> remainingValues;
  private final Map<T, List<U>> domains;

  /**
   * @param domains A set of domains whose values list all the possible values for the
   *                parameter represented by keys.
   */
  public CCSValueTupleSet(Map<T, List<U>> domains) {
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

  public void add(ValueTuple<T, U> constraint) {
    for (T attr : constraint.keySet()) {
      registerConstraintToAttr(constraint, attr);
    }
  }

  /**
   * Registers given constraint to the inverted index.
   *
   * @param constraint A constraint to be indexed.
   * @param attr       An attribute on which the constraint is indexed.
   */
  protected void registerConstraintToAttr(ValueTuple<T, U> constraint, T attr) {
    U value = constraint.get(attr);
    AttrValue<T, U> attrValue = new AttrValue<T, U>(attr, value);
    List<ValueTuple<T, U>> constraintsForAttrValue = invertedIndexForConstraint.get(attrValue);
    if (constraintsForAttrValue == null) {
      constraintsForAttrValue = new LinkedList<ValueTuple<T, U>>();
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

  void newCoveringConstraint(ValueTuple<T, U> constraint, T attr) {
    // //
    // Identify value for attr in the newly found constraint.
    U valueCoveredByConstraint = constraint.get(attr);
    // //
    // Create a list of pairs whose keys are 'attribute values' and values are
    // corresponding constraints.
    // But constraints for the attribute value covered by the newly covered
    // constraint is excluded.
    List<AttrValue<AttrValue<T, U>, ValueTuple<T, U>>> attrValues = new LinkedList<AttrValue<AttrValue<T, U>, ValueTuple<T, U>>>();
    for (U value : this.domains.get(attr)) {
      if (!eq(value, valueCoveredByConstraint)) {
        for (ValueTuple<T, U> cur : this.invertedIndexForConstraint.get(attr)) {
          attrValues.add(new AttrValue<AttrValue<T, U>, ValueTuple<T, U>>(new AttrValue<T, U>(attr, value), cur));
        }
      }
    }
    // //
    // Add a value pair for newly found constraint.
    attrValues.add(new AttrValue<AttrValue<T, U>, ValueTuple<T, U>>(
        new AttrValue<T, U>(attr, valueCoveredByConstraint), constraint));

    // //
    // Iterates over all possible sets that cover all the values of attribute
    // 'attr'.
    CartesianEnumerator<AttrValue<T, U>, ValueTuple<T, U>> enumerator = new CartesianEnumerator<AttrValue<T, U>, ValueTuple<T, U>>(
        attrValues);
    for (List<AttrValue<AttrValue<T, U>, ValueTuple<T, U>>> v : enumerator) {
      ValueTuple<T, U> next = new ValueTuple<T, U>();
      for (AttrValue<AttrValue<T, U>, ValueTuple<T, U>> w : v) {
        ValueTuple<T, U> cur = new ValueTuple<T, U>();
        cur.putAll(w.value());
        cur.remove(attr);
        if ((next = merge(cur, next)) == null) {
          break;
        }
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

  /**
   * Returns a new constraint object which is created by merging this object and
   * <code>another</code> object. If this object and it are not consistent,
   * <code>null</code> will be returned.
   *
   * @param another A constraint object to be merged with this object.
   * @return A merged constraint object.
   */
  public static <T, U>
  ValueTuple<T, U> merge(ValueTuple<T, U> it, ValueTuple<T, U> another) {
    if (another == null) {
      throw new NullPointerException();
    }
    ValueTuple<T, U> ret = new ValueTuple<T, U>();
    ValueTuple<T, U> left = it;
    ValueTuple<T, U> right = another;
    if (it.size() > another.size()) {
      left = another;
      right = it;
    }
    if (!check(left, right)) {
      return null;
    }
    ret.putAll(it);
    ret.putAll(another);
    return ret;
  }

  private static <T, U>
  boolean check(ValueTuple<T, U> left, ValueTuple<T, U> right) {
    for (T key : left.keySet()) {
      if (!right.containsKey(key)) {
        continue;
      }
      if (eq(left.get(key), right.get(key))) {
        continue;
      }
      return false;
    }
    return true;
  }

  private static <U>
  boolean eq(U a, U b) {
    if (a == null) {
      return b == null;
    }
    return a.equals(b);
  }

}
