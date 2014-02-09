package com.github.dakusui.lisj;

import java.math.MathContext;

import com.github.dakusui.jcunit.exceptions.SymbolNotFoundException;

public interface Context extends Cloneable {

	/*
     * Throws an exception if the given name isn't registered. 
	 */
	Object lookup(Symbol name) throws SymbolNotFoundException;
	
	/*
	 * Unlike Java's <code>HashMap.put</code> method, this method returns the newly 
	 * registered <code>value</code>. 
	 */
	Object bind(Symbol symbol, Object value);

	Context createChild();
	
	MathContext bigDecimalMathContext();

	Object add(Object... params);

	Object and(Object... args);
	
	Object any();

	Object bigDecimal(Object num);

	Object bigInteger(Object num);

	Object byteValue(Object num);

	Object same(Object obj, Object another);

	Object contains(Object obj, String str);

	Object div(Object... params);

	Object doubleValue(Object num);

	Object ne(Object obj, Object another);

	Object floatValue(Object num);

	Object ge(Object obj, Object another);

	Object get(Object obj, Object attrName);

	Object gt(Object obj, Object another);

	Object intValue(Object num);

	Object is(Object obj, Object arg);

	Object isoneof(Object obj, Object... args);

	Object le(Object obj, Object another);

	Object longValue(Object num);

	Object lt(Object obj, Object another);

	Object matches(Object attrName, String regex);

	Object max(Object... params);

	Object min(Object... params);

	Object mul(Object... params);

	Object not(Object target);

	Object or(Object... args);

	Object set(Object obj, Object attrName, Object value);

	Object shortValue(Object num);

	Object sub(Object... params);

	Symbol $(String name);

	Object cond(Object... whens);

	Object when(Object pred, Object... statements);

	Object assign(Symbol symbol, Object value);

	Object print(Object s);

	Object loop(Object cond, Object... forms);

	Object progn(Object... forms);

	Object format(Object format, Object... args);

	Object lambda(Symbol[] params, Object... funcBody);

	Object lambda(Symbol param, Object... funcBody);

	Symbol[] $(String... names);

	Object eval(Object... args);
	
	Object isinstanceof(Object obj, Object clazz);

	Object eq(Object obj, Object arg);

	Object concat(Object separator, Object... args);

	Object outFieldNames(Object obj);
}
