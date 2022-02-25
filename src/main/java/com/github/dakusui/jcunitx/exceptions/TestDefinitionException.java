package com.github.dakusui.jcunitx.exceptions;

import com.github.dakusui.jcunitx.core.AArray;
import com.github.dakusui.jcunitx.factorspace.Constraint;

import java.util.List;

import static java.lang.String.format;

/**
 * Hello world.
 *
 * Indicates user's artifact (Typically a test class, annotated with `@RunWith(JCUnit8.class)`), is invalid.
 *
 * [ditaa]
 * ----
 * +-----+      +-----+
 * |hello|<>--->|world|
 * +-----+      +-----+
 * ----
 */
public class TestDefinitionException extends BaseException {
  private TestDefinitionException(String message) {
    super(message);
  }

  /**
   * Creates an instance of this class.
   *
   * @param message An error message
   * @param e A nested exception
   */
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

  public static TestDefinitionException noParameterFound() {
    throw new TestDefinitionException("No parameter is found.");
  }
}
