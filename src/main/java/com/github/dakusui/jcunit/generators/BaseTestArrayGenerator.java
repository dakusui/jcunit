package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.GeneratorParameters;
import com.github.dakusui.jcunit.core.GeneratorParameters.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

public abstract class BaseTestArrayGenerator<T> implements
		TestArrayGenerator<T> {
	/**
	 * A logger object.
	 */
	private static final Logger LOGGER = LoggerFactory
			.getLogger(BaseTestArrayGenerator.class);

	protected LinkedHashMap<T, Object[]> domains = null;
	private long size = -1;
	private long cur = -1;

	protected Value[] params;

	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		if (size < 0 || this.cur < 0)
			throw new IllegalStateException();
		return cur < size;
	}

	@Override
	public Iterator<Map<T, Object>> iterator() {
		return this;
	}

	@Override
	public Map<T, Object> next() {
		if (cur >= size)
			throw new NoSuchElementException();
		Map<T, Object> ret = get(cur);
		cur++;
		return ret;
	}

	@Override
	final public void init(GeneratorParameters.Value[] params,
	                 LinkedHashMap<T, Object[]> domains) {
		// //
		// TODO: We shouldn't need to create another linked hash map anymore.
		this.domains = new LinkedHashMap<T, Object[]>();
		this.domains.putAll(domains);
		List<T> ignoredKeys = new LinkedList<T>();
		for (T f : this.domains.keySet()) {
			Object[] d = this.domains.get(f);
			if (d.length == 0) {
				ignoredKeys.add(f);
				LOGGER.warn(
						"The domain of '{}' is empty. This parameter will be ignored.", f);
				continue;
			}
		}
		for (T f : ignoredKeys) {
			this.domains.remove(f);
		}

		this.params = params;
		this.size = initializeTestCases(params, domains);
		this.cur = 0;
	}

	@Override
	public Map<T, Object> get(long cur) {
		Map<T, Object> ret = new LinkedHashMap<T, Object>();
		for (T f : this.domains.keySet()) {
			Object[] values = domains.get(f);
			ret.put(f, values[getIndex(f, cur)]);
		}
		return ret;
	}

	@Override
	public Object[] getDomain(T key) {
		return this.domains.get(key);
	}

	public List<T> getKeys() {
		List<T> ret = new ArrayList<T>(this.domains.size());
		for (T k : this.domains.keySet()) {
			ret.add(k);
		}
		return ret;
	}

	@Override
	public long size() {
		if (this.size < 0)
			throw new IllegalStateException();
		return this.size;
	}

	/**
	 * Implementation of this method must return a number of test cases to be executed in total.
	 *
	 * @return A number of test cases
	 */
	abstract protected long initializeTestCases(GeneratorParameters.Value[] params,
	                                           LinkedHashMap<T, Object[]> domains);
}
