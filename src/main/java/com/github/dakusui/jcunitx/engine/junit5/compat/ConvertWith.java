package com.github.dakusui.jcunitx.engine.junit5.compat;

public @interface ConvertWith {
  Class<? extends ArgumentConverter> value();
}
