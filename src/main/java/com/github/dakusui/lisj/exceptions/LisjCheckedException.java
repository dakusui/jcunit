package com.github.dakusui.lisj.exceptions;

/**
 */
public class LisjCheckedException extends Exception {
	public LisjCheckedException(String message, Throwable cause) {
		super(message, cause);
	}

	public LisjCheckedException(String message) {
		super(message);
	}
}
