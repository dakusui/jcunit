package com.github.dakusui.jcunit.auto.encoder;


public abstract class BaseObjectEncoder implements ObjectEncoder {

	protected String createMessage_FailedToDecodeObject(ClassNotFoundException e) {
		String msg = String.format(
			"%s:Failed to decode object:'%')", 
			this.getClass().getName(),
			e.getMessage()
		);
		return msg;
	}
}
