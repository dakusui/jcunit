package com.github.dakusui.jcunit8.exceptions;

import com.github.dakusui.jcunit.core.tuples.AArray;
import com.github.dakusui.jcunit8.factorspace.Constraint;

import java.util.List;

import static java.lang.String.format;

/**
 * Indicates user's artifact (Typically test classes, annotated with
 * {@literal @}{@code RunWith(JCUnit.class)}), is invalid.
 */
public class TestDefinitionException extends BaseException {
  private TestDefinitionException(String message) {
    super(message);
  }

  private TestDefinitionException(String message, Throwable e) {
    super(message, e);
  }

  public static TestDefinitionException wrap(Throwable t) {
    throw new TestDefinitionException(format("Test definition is not valid: %s", t.getMessage()), t);
  }

  public static TestDefinitionException impossibleConstraint(List<Constraint> constraints) {
    throw new TestDefinitionException(format("Constraints '%s' did not become true.", constraints));
  }

  public static TestDefinitionException failedToCover(String factorName, List<Object> factorLevels, AArray tuple) {
    throw new TestDefinitionException(format("Factor '%s' doesn't have any valid level '%s' for tuple '%s'", factorName, factorLevels, tuple));
  }

  public static TestDefinitionException parameterWithoutAnnotation(String methodName) {
    throw new TestDefinitionException(String.format("Method parameter not annotated with @From is found at '%s'", methodName));
  }

  public static  TestDefinitionException noParameterFound() {
    throw new TestDefinitionException("No parameter is found.");
  }
}
