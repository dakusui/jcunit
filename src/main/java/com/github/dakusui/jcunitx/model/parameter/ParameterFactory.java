package com.github.dakusui.jcunitx.model.parameter;

public interface ParameterFactory {
  Parameter<?> create(String name);
}
