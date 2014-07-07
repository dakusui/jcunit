package com.github.dakusui.jcunit.constraints.constraintmanagers.ccs;

import com.github.dakusui.enumerator.tuple.AttrValue;
import com.github.dakusui.enumerator.tuple.CartesianEnumerator;
import com.github.dakusui.jcunit.core.Tuple;

import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CCSValueTupleSet {
	Map<AttrValue<String, Object>, List<Tuple>> invertedIndexForConstraint = new HashMap<AttrValue<String, Object>, List<Tuple>>();
	Map<String, Set<AttrValue<String, Object>>> remainingValues = new HashMap<String, Set<AttrValue<String, Object>>>();
	private final Map<String, List<Object>> domains;

	/**
	 * @param domains A set of domains whose values list all the possible values for the
	 *                parameter represented by keys.
	 */
	public CCSValueTupleSet(Map<String, List<Object>> domains) {
		this.domains = domains;
		for (String attr : domains.keySet()) {
			List<Object> currentDomain = domains.get(attr);
			Set<AttrValue<String, Object>> attrValues = new HashSet<AttrValue<String, Object>>();
			this.remainingValues.put(attr, attrValues);
			for (Object value : currentDomain) {
				attrValues.add(new AttrValue<String, Object>(attr, value));
			}
		}
	}

	public void add(Tuple constraint) {
		for (String attr : constraint.keySet()) {
			registerConstraintToAttr(constraint, attr);
		}
	}

	/**
	 * Registers given constraint to the inverted index.
	 *
	 * @param constraint A constraint to be indexed.
	 * @param attr       An attribute on which the constraint is indexed.
	 */
	protected void registerConstraintToAttr(Tuple constraint, String attr) {
		Object value = constraint.get(attr);
		AttrValue<String, Object> attrValue = new AttrValue<String, Object>(attr, value);
		List<Tuple> constraintsForAttrValue = invertedIndexForConstraint.get(attrValue);
		if (constraintsForAttrValue == null) {
			constraintsForAttrValue = new LinkedList<Tuple>();
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

	void newCoveringConstraint(Tuple constraint, String attr) {
		// //
		// Identify value for attr in the newly found constraint.
		Object valueCoveredByConstraint = constraint.get(attr);
		// //
		// Create a list of pairs whose keys are 'attribute values' and values are
		// corresponding constraints.
		// But constraints for the attribute value covered by the newly covered
		// constraint is excluded.
		List<AttrValue<AttrValue<String, Object>, Tuple>> attrValues = new LinkedList<AttrValue<AttrValue<String, Object>, Tuple>>();
		for (Object value : this.domains.get(attr)) {
			if (!eq(value, valueCoveredByConstraint)) {
				for (Tuple cur : this.invertedIndexForConstraint.get(attr)) {
					attrValues.add(new AttrValue<AttrValue<String, Object>, Tuple>(new AttrValue<String, Object>(attr, value), cur));
				}
			}
		}
		// //
		// Add a value pair for newly found constraint.
		attrValues.add(new AttrValue<AttrValue<String, Object>, Tuple>(
				new AttrValue<String, Object>(attr, valueCoveredByConstraint), constraint));

		// //
		// Iterates over all possible sets that cover all the values of attribute
		// 'attr'.
		CartesianEnumerator<AttrValue<String, Object>, Tuple> enumerator = new CartesianEnumerator<AttrValue<String, Object>, Tuple>(
				attrValues);
		for (List<AttrValue<AttrValue<String, Object>, Tuple>> v : enumerator) {
			Tuple next = new Tuple();
			for (AttrValue<AttrValue<String, Object>, Tuple> w : v) {
				Tuple cur = new Tuple();
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

	void newlyCoveredParameterFound(String t) {

	}

	Iterable<Object> allPossibleValuesOf(String attr) {
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
	Tuple merge(Tuple it, Tuple another) {
		if (another == null) {
			throw new NullPointerException();
		}
		Tuple ret = new Tuple();
		Tuple left = it;
		Tuple right = another;
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
	boolean check(Tuple left, Tuple right) {
		for (String key : left.keySet()) {
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

	private static boolean eq(Object a, Object b) {
		if (a == null) {
			return b == null;
		}
		return a.equals(b);
	}

}
