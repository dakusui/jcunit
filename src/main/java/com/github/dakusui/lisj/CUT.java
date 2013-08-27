package com.github.dakusui.lisj;

public class CUT extends Exception {
	private static final long serialVersionUID = 1L;
	private Object value;
	private Form source;
	CUT(Form source, Object value) {
		this.source = source;
		this.value = value; 
	}
	
	public Object value() { 
		return this.value; 
	}
	
	public Form source() {
		return this.source;
	}
}