package com.github.dakusui.jcunit.compat.core;

import com.github.dakusui.jcunit.exceptions.JCUnitCheckedException;

public interface DomainGenerator {
  Object[] domain() throws JCUnitCheckedException;
}