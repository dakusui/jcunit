package com.github.dakusui.jcunitx.runners.junit5;

import org.junit.jupiter.api.extension.*;

public class JCUnitX implements ParameterResolver, ExecutionCondition {
  @Override
  public ConditionEvaluationResult evaluateExecutionCondition(ExtensionContext extensionContext) {
    return ConditionEvaluationResult.enabled("Default");
  }

  @Override
  public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return false;
  }

  @Override
  public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext) throws ParameterResolutionException {
    return null;
  }
}
