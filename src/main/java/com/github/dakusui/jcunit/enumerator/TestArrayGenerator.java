package com.github.dakusui.jcunit.enumerator;

import java.util.Iterator;
import java.util.Map;

public interface TestArrayGenerator<T, U> extends Iterator<Map<T, U>>, Iterable<Map<T, U>>{

	public Map<T, U[]> getDomains();
	
	public void init(Map<T, U[]> domains);
	
}
