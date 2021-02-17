package com.github.dakusui.jcunitx.engine.junit5.compat;

public @interface AggregateWith {
  Class<? extends ArgumentsAggregator> value();
}
