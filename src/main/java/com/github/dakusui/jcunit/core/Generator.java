package com.github.dakusui.jcunit.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.github.dakusui.jcunit.enumerator.TestArrayGenerator;


@Retention(RetentionPolicy.RUNTIME)
public @interface Generator {

	@SuppressWarnings("rawtypes")
	Class<? extends TestArrayGenerator> value();

}
