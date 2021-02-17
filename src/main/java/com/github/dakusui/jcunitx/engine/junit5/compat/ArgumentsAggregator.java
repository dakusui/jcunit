package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.junit.jupiter.api.extension.ParameterContext;

public interface ArgumentsAggregator {
  Object aggregateArguments(ArgumentsAccessor accessor, ParameterContext context)
      throws ArgumentsAggregationException;
}
