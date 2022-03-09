package com.github.dakusui.jcunitx.metamodel.parameters;

import java.util.Optional;

public interface ExecutionContext {
  <T> Optional<T> resultOf(String methodName, int index);
}
