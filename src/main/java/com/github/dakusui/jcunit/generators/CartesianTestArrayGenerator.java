package com.github.dakusui.jcunit.generators;

import java.util.LinkedHashMap;
import java.util.Map;

public class CartesianTestArrayGenerator<T, U> extends BaseTestArrayGenerator<T, U> {
	@Override
	public void init(Map<T, U[]> domains) {
		super.init(domains);
		if (this.domains == null) throw new NullPointerException();
		assert this.size < 0;
		assert this.cur < 0;
		size = 1;
		for (T f : this.domains.keySet()) {
			U[] d = this.domains.get(f);
			size *= d.length;
		}
		cur = 0;
	}

	@Override
	protected Map<T, U> get(long cur) {
		Map<T, U> ret = new LinkedHashMap<T, U>();
		long div = cur;
		for (T f : this.domains.keySet()) {
			U[] values = domains.get(f);
			int index = (int) (div % values.length);
			ret.put(f, values[index]);
			
			div = div / values.length;
		}
		return ret;
	}
}
