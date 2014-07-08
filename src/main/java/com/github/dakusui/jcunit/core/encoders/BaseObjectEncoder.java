package com.github.dakusui.jcunit.core.encoders;

import com.github.dakusui.jcunit.core.annotations.FactorField;
import org.mockito.Mock;

public abstract class BaseObjectEncoder implements ObjectEncoder {

  protected String createMessage_FailedToDecodeObject(
      ClassNotFoundException e) {
    String msg = String.format("%s:Failed to decode object:'%')", this
        .getClass().getName(), e.getMessage());
    return msg;
  }

	@Mock
			@FactorField()
	BaseObjectEncoder t;


	public void t() {}
}
