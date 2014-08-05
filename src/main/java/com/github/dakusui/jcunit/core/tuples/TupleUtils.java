package com.github.dakusui.jcunit.core.tuples;

import com.github.dakusui.enumerator.CartesianEnumeratorAdaptor;
import com.github.dakusui.enumerator.Combinator;
import com.github.dakusui.enumerator.Domains;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.Factor;
import com.github.dakusui.jcunit.exceptions.SavedObjectBrokenException;

import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

public class TupleUtils {
	public static class CartesianTuples extends CartesianEnumeratorAdaptor<Tuple, String, Object> {

		private final Tuple base;

		protected CartesianTuples(Tuple base, final Factor... factors) {
			super(new Domains<String, Object>() {
				@Override
				public List<String> getDomainNames() {
					List<String> ret = new ArrayList<String>(factors.length);
					for (Factor f : factors) {
						ret.add(f.name);
					}
					return ret;
				}

				@Override
				public List<Object> getDomain(String s) {
					Utils.checknotnull(s);
					for (Factor f : factors) {
						if (s.equals(f.name)) {
							return f.levels;
						}
					}
					return null;
				}
			});
			this.base = Utils.checknotnull(base);
		}

		@Override
		protected Tuple createMap() {
			return base.cloneTuple();
		}
	}

	public static CartesianTuples enumerateCartesianProduct(final Tuple base, Factor... factors) {
		Utils.checknotnull(base);
		return new CartesianTuples(base, factors);
	}

	public static Set<Tuple> subtuplesOf(
			Tuple tuple, int strength) {
		Utils.checknotnull(tuple);
		Utils.checkcond(strength >= 0 && strength <= tuple.size());
		Set<Tuple> ret = new HashSet<Tuple>();
		Combinator<String> c = new Combinator<String>(
				new LinkedList<String>(tuple.keySet()), strength);
		for (List<String> keys : c) {
			Tuple cur = new TupleImpl();
			for (String k : keys) {
				cur.put(k, tuple.get(k));
			}
			ret.add(cur);
		}
		return ret;
	}

	public static Set<Tuple> subtuplesOf(Tuple tuple) {
		Utils.checknotnull(tuple);
		Set<Tuple> ret = new HashSet<Tuple>();
		int sz = tuple.size();
		for (int i = 0; i <= sz; i++) {
			ret.addAll(subtuplesOf(tuple, sz - i));
		}
		return ret;
	}

	/**
	 * Returns {@code true} if {@code t} is a sub-tuple of {@code u}, {@code false} otherwise.
	 */
	public static boolean isSubtupleOf(Tuple t, Tuple u) {
		Utils.checknotnull(t);
		Utils.checknotnull(u);
		return t.isSubtupleOf(u);
	}

	public static Tuple unmodifiableTuple(Tuple tuple) {
		Utils.checknotnull(tuple);
		return new Tuple.Builder().putAll(tuple).setUnmodifiable(true).build();
	}

	public static String toString(Collection<Tuple> tuples) {
		StringBuilder b = new StringBuilder();
		b.append('[');
		boolean firstTime = true;
		for (Tuple t : tuples) {
			if (!firstTime) b.append(",");
			b.append(toString(t));
			firstTime = false;
		}
		b.append(']');
		return b.toString();
	}

	public static String toString(Tuple tuple) {
		Utils.checknotnull(tuple);
		return tupleToString(tuple);
	}

	private static String escape(Object v) {
		String ret = v.toString();
		ret = ret.replaceAll("\\\\", "\\\\\\\\");
		ret = ret.replaceAll("\"", "\\\\\"");
		return ret;
	}

	private static String arrToString(Object v) {
		int len = Array.getLength(v);
		StringBuilder b = new StringBuilder();
		b.append('[');
		for (int i = 0; i < len; i++) {
			if (i > 0) {
				b.append(',');
			}
			b.append(valueToString(Array.get(v, i)));
		}
		b.append(']');
		return b.toString();
	}

	private static String tupleToString(Tuple tuple) {
		StringBuilder b = new StringBuilder();
		Set<String> keySet = tuple.keySet();
		b.append('{');
		boolean firstTime = true;
		for (String k : keySet) {
			if (!firstTime) {
				b.append(',');
			}
			Object v = tuple.get(k);
			b.append(String.format("\"%s\":%s", escape(k),
					valueToString(v)
			));
			firstTime = false;
		}
		b.append('}');
		return b.toString();
	}

	private static String valueToString(Object v) {
		return v == null ? null
				: v instanceof Tuple ? tupleToString((Tuple) v)
				: v instanceof Number ? v.toString()
				: v.getClass().isArray() ? arrToString(v)
				: String.format("\"%s\"", escape(v));
	}

	public static void save(Tuple tuple, OutputStream os) {
		Utils.save(tuple, os);
	}

	public static Tuple load(InputStream is) {
		Object obj = Utils.load(is);
		if (obj instanceof Tuple) {
			return (Tuple) obj;
		}
		throw new SavedObjectBrokenException(String.format("Saved object wasn't a tuple (%s)", obj.getClass().getCanonicalName()), null);
	}
}
