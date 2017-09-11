package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit8.factorspace.Constraint;
import com.github.dakusui.jcunit8.factorspace.Factor;
import com.github.dakusui.jcunit8.factorspace.TestPredicate;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static com.github.dakusui.jcunit8.core.Utils.unique;
import static com.github.dakusui.jcunit8.pipeline.stages.Generator.VOID;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;

public class FsmDecomposer<SUT> extends FsmTupleAccessor<SUT> {
  private final List<Factor>     factors;
  private final List<Constraint> constraints;
  private final int              maxActionParams;

  public FsmDecomposer(String name, FiniteStateMachine<SUT> model, int scenarioLength) {
    super(name, model, scenarioLength);
    this.maxActionParams = model.actions().stream()
        .mapToInt(value -> value.parameters().size())
        .max()
        .orElse(0);
    this.constraints = buildConstraints();
    this.factors = buildFactors();
  }

  public List<Factor> getFactors() {
    return this.factors;
  }

  public List<Constraint> getConstraints() {
    return this.constraints;
  }

  private List<Factor> buildFactors() {
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
          new ArrayList<Object>(union(
              model.actions().stream()
                  .map(Action::parameters)
                  .collect(Collectors.toList()),
              j)) {{
            add(VOID);
          }}.toArray()
      ));
    }
    return ret;
  }

  private List<Object> union(List<Parameters> parametersList, int j) {
    return unique(
        parametersList.stream()
            .filter((Parameters factors) -> factors.size() > j)
            .flatMap((Parameters factors) -> factors.get(j).getLevels().stream())
            .collect(toList())
    );
  }

  private List<Constraint> buildConstraints() {
    return IntStream.range(0, this.scenarioLength)
        .mapToObj(i -> asList(
            createConstraintForStateActionValidity(i),
            createConstraintForActionArgs(i),
            createConstraintForActionStateValidity(i),
            createConstraintForNormality(i)
        ))
        .flatMap(Collection::stream)
        .collect(toList());
  }

  private Constraint createConstraintForActionArgs(int i) {
    return new Constraint() {
      @Override
      public String getName() {
        return toString();
      }

      @Override
      public boolean test(Tuple tuple) {
        Action action = getActionFromTuple(tuple, i);
        for (int j = 0; j < action.numParameterFactors(); j++) {
          Object level = getActionArgFromTuple(tuple, i, j);
          if (Objects.equals(level, VOID))
            return false;
          if (!action.parameters().get(j).getLevels().contains(level)) {
            return false;
          }
        }
        for (int j = action.numParameterFactors(); j < maxActionParams; j++) {
          if (getActionArgFromTuple(tuple, i, j) != VOID)
            return false;
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

      @Override
      public String toString() {
        return TestPredicate.toString(this);
      }
    };
  }

  private Constraint createConstraintForStateActionValidity(int i) {
    return new Constraint() {
      @Override
      public String getName() {
        return toString();
      }

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

      @Override
      public String toString() {
        return TestPredicate.toString(this);
      }
    };
  }

  private Constraint createConstraintForNormality(int i) {
    return new Constraint() {
      @Override
      public String getName() {
        return toString();
      }

      @Override
      public boolean test(Tuple tuple) {
        State<SUT> state = getStateFromTuple(tuple, i);
        Action<SUT> action = getActionFromTuple(tuple, i);
        Args args = getActionArgsFromTuple(tuple, i);
        //noinspection SimplifiableIfStatement
        if (args.containsVoid())
          return false;
        return state.expectation(action, args).getType() == OutputType.VALUE_RETURNED;
      }

      @Override
      public List<String> involvedKeys() {
        return Stream.concat(
            Stream.of(
                composeStateFactorName(name, i),
                composeActionFactorName(name, i)
            ),
            IntStream.range(0, maxActionParams).mapToObj((int j) -> composeActionParamFactorName(name, i, j))
        ).collect(toList());
      }

      @Override
      public String toString() {
        return TestPredicate.toString(this);
      }
    };
  }

  private Constraint createConstraintForActionStateValidity(int i) {
    return new Constraint() {
      @Override
      public String getName() {
        return toString();
      }

      @Override
      public boolean test(Tuple tuple) {
        //noinspection SimplifiableConditionalExpression
        return allPossibleEdges(
            sutState -> true,
            sutAction -> sutAction.equals(getActionFromTuple(tuple, i)),
            sutState -> i + 1 < scenarioLength ?
                sutState.equals(getStateFromTuple(tuple, i + 1)) :
                true
        ).findFirst().isPresent();
      }

      @Override
      public List<String> involvedKeys() {
        return i + 1 < scenarioLength ?
            asList(composeActionFactorName(name, i), composeStateFactorName(name, i + 1)) :
            Collections.singletonList(composeActionFactorName(name, i));
      }

      @Override
      public String toString() {
        return TestPredicate.toString(this);
      }
    };
  }
}
