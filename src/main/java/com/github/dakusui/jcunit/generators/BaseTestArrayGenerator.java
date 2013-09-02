package com.github.dakusui.jcunit.generators;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class BaseTestArrayGenerator<T, U> implements TestArrayGenerator<T, U> {
	/**
	 * A logger object.
	 */
	private static final Logger LOGGER = LoggerFactory.getLogger(BaseTestArrayGenerator.class);

	protected Map<T, U[]> domains = null;
	protected long size = -1;
	protected long cur = -1;
	
	@Override
	public void remove() {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean hasNext() {
		if (size < 0 || this.cur < 0) throw new IllegalStateException();
		return cur < size;
	}

	@Override
	public Iterator<Map<T, U>> iterator() {
		return this;
	}

	@Override
	public Map<T, U> next() {
		if (cur >= size) throw new NoSuchElementException();
		Map<T, U> ret = get(cur);
		cur ++;
		return ret;
	}

	@Override
	public void init(Map<T, U[]> domains) {
		this.domains = new LinkedHashMap<T, U[]>();
		this.domains.putAll(domains);
		List<T> ignoredKeys = new LinkedList<T>();
		for (T f : this.domains.keySet()) {
			U[] d = this.domains.get(f);
			if (d.length == 0) {
				ignoredKeys.add(f);
				LOGGER.warn("The domain of '{}' is empty. This parameter will be ignored.", f);
				continue;
			}
		}
		for (T f : ignoredKeys) {
			this.domains.remove(f);
		}

		this.size = -1;
		this.cur = -1;
	}

	protected abstract Map<T, U> get(long cur);

	@Override
	public Map<T, U[]> getDomains() {
		return this.domains;
	}
}
