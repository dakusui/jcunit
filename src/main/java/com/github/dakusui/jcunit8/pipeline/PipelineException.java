package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit8.exceptions.FrameworkException;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.ParameterSpace;

import java.util.List;
import java.util.Set;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import static java.lang.String.format;
import static java.util.Collections.disjoint;

public class PipelineException extends FrameworkException {
  private PipelineException(String format) {
    super(format);
  }

  public static void checkIfStrengthIsInRange(int strength, List<String> attributeNames) {
    checkCondition(
        0 < strength && strength <= attributeNames.size(),
        PipelineException::new,
        () -> format("Given strength '%s' is not in appropriate range (0, %d]", strength, attributeNames.size())
    );
  }

  static void checkIfNoNonSimpleParameterIsInvolvedByAnyConstraint(ParameterSpace parameterSpace) {
    Set<String> nonSimpleParameters = parameterSpace.getParameterNames().stream()
        .filter(s -> !(parameterSpace.getParameter(s) instanceof Parameter.Simple))
        .collect(Collectors.toSet());
    Predicate<Constraint> checkNoNonSimpleParameterInvolved =
        (Constraint constraint) -> disjoint(nonSimpleParameters, constraint.involvedKeys());
    checkCondition(
        parameterSpace.getConstraints().stream().allMatch(checkNoNonSimpleParameterInvolved),
        PipelineException::new,
        () -> format("A constraint that involves non-simple parameter was found:%s",
            parameterSpace.getConstraints().stream().filter(checkNoNonSimpleParameterInvolved)
        ));
  }
}
