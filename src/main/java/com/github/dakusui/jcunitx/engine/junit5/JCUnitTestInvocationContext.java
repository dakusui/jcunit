package com.github.dakusui.jcunitx.engine.junit5;

import org.junit.jupiter.api.extension.Extension;
import org.junit.jupiter.api.extension.TestTemplateInvocationContext;

import java.util.List;

import static java.util.Collections.singletonList;

public class JCUnitTestInvocationContext implements TestTemplateInvocationContext {
  private final JCUnitTestNameFormatter formatter;
  private final JCUnitTestMethodContext methodContext;
  private final Object[] arguments;

  JCUnitTestInvocationContext(JCUnitTestNameFormatter formatter,
      JCUnitTestMethodContext methodContext, Object[] arguments) {
    this.formatter = formatter;
    this.methodContext = methodContext;
    this.arguments = arguments;
  }

  @Override
  public String getDisplayName(int invocationIndex) {
    return this.formatter.format(invocationIndex, this.arguments);
  }

  @Override
  public List<Extension> getAdditionalExtensions() {
    return singletonList(new JCUnitTestParameterResolver(this.methodContext, this.arguments));
  }
}
