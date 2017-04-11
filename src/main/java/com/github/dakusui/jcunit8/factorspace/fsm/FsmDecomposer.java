package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.Action;
import com.github.dakusui.jcunit.fsm.FiniteStateMachine;
import com.github.dakusui.jcunit.fsm.Parameters;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.core.Utils.unique;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class FsmDecomposer<SUT> extends FsmTupleAccessor<SUT> {

  private final List<Factor>     factors;
  private final List<Constraint> constraints;
  private final int              maxActionParams;

  public FsmDecomposer(String name, FiniteStateMachine<SUT> model, int scenarioLength) {
    super(name, model, scenarioLength);
    this.factors = decompose();
    this.constraints = generateConstraints();
    this.maxActionParams = model.actions().stream()
        .mapToInt(value -> value.parameters().size())
        .max()
        .orElse(0);
  }

  public List<Factor> getFactors() {
    return this.factors;
  }

  public List<Constraint> getConstraints() {
    return this.constraints;
  }

  private List<Factor> decompose() {
    return range(0, this.scenarioLength).mapToObj(
        (int i) -> Stream.concat(Stream.of(
            createFactorForState(name, i),
            createFactorForAction(name, i)),
            createFactorsForActionParams(name, i).stream()
        ).collect(toList()))
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private Factor createFactorForState(String name, int i) {
    return Factor.create(
        composeStateFactorName(name, i),
        model.states().toArray()
    );
  }

  private Factor createFactorForAction(String name, int i) {
    return Factor.create(
        composeActionFactorName(name, i),
        model.actions().toArray()
    );
  }

  private List<Factor> createFactorsForActionParams(String name, int i) {
    List<Factor> ret = new LinkedList<>();
    for (int j = 0; j < this.maxActionParams; j++) {
      ret.add(Factor.create(
          composeActionParamFactorName(name, i, j),
          union(
              model.actions().stream()
                  .map(Action::parameters)
                  .collect(Collectors.toList()),
              j)
      ));
    }
    return ret;
  }

  private Object[] union(List<Parameters> parametersList, int j) {
    return unique(
        parametersList.stream()
            .flatMap(factors -> factors.get(j).getLevels().stream())
            .collect(toList())
    ).toArray();
  }

  private List<Constraint> generateConstraints() {
    return IntStream.range(0, this.scenarioLength)
        .mapToObj(i -> asList(
            createConstraintForStateActionValidity(i),
            createConstraintForActionArgs(i),
            createConstraintForActionStateValidity(i)
        ))
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private Constraint createConstraintForActionArgs(int i) {
    return new Constraint() {
      @Override
      public boolean test(Tuple testObject) {
        Action action = getActionFromTuple(testObject, i);
        for (int j = 0; j < maxActionParams; j++) {
          if (j < action.numParameterFactors()) {
            if (!action.parameters().get(j).getLevels().contains(getActionArgFromTuple(testObject, i, j))) {
              return false;
            }
          } else {
            if (getActionArgFromTuple(testObject, i, j) != Parameters.VOID)
              return false;
          }
        }
        return true;
      }

      @Override
      public List<String> involvedKeys() {
        return Stream.concat(
            Stream.of(composeActionFactorName(name, i)),
            IntStream.range(0, maxActionParams).mapToObj((int j) -> composeActionParamFactorName(name, i, j))
        ).collect(toList());
      }
    };
  }

  private Constraint createConstraintForStateActionValidity(int i) {
    return new Constraint() {
      @Override
      public boolean test(Tuple testObject) {
        return allPossibleEdges(
            sutState -> sutState.equals(getStateFromTuple(testObject, i)),
            sutAction -> sutAction.equals(getActionFromTuple(testObject, i)),
            sutState -> true
        ).findFirst().isPresent();
      }

      @Override
      public List<String> involvedKeys() {
        return asList(
            composeStateFactorName(name, i),
            composeActionFactorName(name, i)
        );
      }
    };
  }

  private Constraint createConstraintForActionStateValidity(int i) {
    return new Constraint() {
      @Override
      public boolean test(Tuple testObject) {
        //noinspection SimplifiableConditionalExpression
        return allPossibleEdges(
            sutState -> true,
            sutAction -> sutAction.equals(getActionFromTuple(testObject, i)),
            sutState -> i + 1 < scenarioLength ?
                sutState.equals(getStateFromTuple(testObject, i + 1)) :
                true
        ).findFirst().isPresent();
      }

      @Override
      public List<String> involvedKeys() {
        return i + 1 < scenarioLength ?
            asList(composeActionFactorName(name, i), composeStateFactorName(name, i + 1)) :
            Collections.singletonList(composeActionFactorName(name, i));
      }
    };
  }
}
