package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

public interface DomainGenerator {
	Object[] domain() throws JCUnitException;
}