package com.github.dakusui.jcunit.exceptions;

public class JCUnitException extends Exception {
	/**
	 * A serial version UID. 
	 */
	private static final long serialVersionUID = -8729834127235486228L;

	public JCUnitException() {}
	
	public JCUnitException(String msg, Throwable e) {
		super(msg, e);
	}
}
