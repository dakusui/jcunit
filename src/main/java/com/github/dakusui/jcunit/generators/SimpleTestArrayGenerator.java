package com.github.dakusui.jcunit.generators;

import java.util.LinkedHashMap;
import java.util.Map;

public class SimpleTestArrayGenerator<T, U> extends BaseTestArrayGenerator<T, U>{
	@Override
	public void init(Map<T, U[]> domains) {
		super.init(domains);
		if (this.domains == null) throw new NullPointerException();
		assert this.size < 0;
		assert this.cur < 0;
		
		this.size = 1;
		for (T f : this.domains.keySet()) {
			this.size += Math.max(0, this.domains.get(f).length - 1); 
		}
		this.cur = 0;
	}

	@Override
	protected Map<T, U> get(long cur) {
		Map<T, U> ret = new LinkedHashMap<T, U>();
		////
		// Initialize the returned map with the default values.
		for (T f : this.domains.keySet()) {
			U[] values = domains.get(f);
			////
			// if 'None' domain is specified, the size of the values array will be 0.
			if (values.length > 0) ret.put(f,  values[0]);
		}
		if (cur == 0) return ret;
		cur--;
		for (T f : this.domains.keySet()) {
			long index = cur;
			U[] d = domains.get(f);
			if ((cur -= (d.length - 1)) < 0) {
				ret.put(f, d[(int)index + 1]);
				break;
			}
		}
		return ret;
	}
}
