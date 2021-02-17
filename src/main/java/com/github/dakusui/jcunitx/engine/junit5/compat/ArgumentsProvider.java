package com.github.dakusui.jcunitx.engine.junit5.compat;

import org.junit.jupiter.api.extension.ExtensionContext;

import java.util.stream.Stream;

public interface ArgumentsProvider {
  Stream<? extends Arguments> provideArguments(ExtensionContext context) throws Exception;
}
