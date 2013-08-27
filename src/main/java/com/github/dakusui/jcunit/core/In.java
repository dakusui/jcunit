package com.github.dakusui.jcunit.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface In {
	static enum Domain {
		Default,
		Method,
		None
	}
	
	public Domain domain() default Domain.Default;
}
