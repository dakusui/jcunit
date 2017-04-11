package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit8.core.StreamableCartesianator;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

class FsmTupleAccessor<SUT> {
  final FiniteStateMachine<SUT> model;
  final String                  name;
  final int                     scenarioLength;

  FsmTupleAccessor(String name, FiniteStateMachine<SUT> model, int scenarioLength) {
    this.model = model;
    this.name = name;
    this.scenarioLength = scenarioLength;
  }

  String composeStateFactorName(String name, int i) {
    return format("%s:STATE:%d", name, i);
  }

  String composeActionFactorName(String name, int i) {
    return format("%s:ACTION:%d", name, i);
  }

  String composeActionParamFactorName(String name, int i, int j) {
    return format("%s:ACTION_PARAM:%d-%d", name, i, j);
  }

  State<SUT> getStateFromTuple(Tuple tuple, int i) {
    //noinspection unchecked
    return (State<SUT>) tuple.get(composeStateFactorName(name, i));
  }

  Action<SUT> getActionFromTuple(Tuple tuple, int i) {
    //noinspection unchecked
    return (Action<SUT>) tuple.get(composeActionFactorName(name, i));
  }

  Args getActionArgsFromTuple(Tuple tuple, int i) {
    Args ret = new Args(
        IntStream.range(0, getActionFromTuple(tuple, i).numParameterFactors())
            .mapToObj(j -> getActionArgFromTuple(tuple, i, j))
            .toArray()
    );
    return ret;
  }

  Object getActionArgFromTuple(Tuple tuple, int i, int j) {
    //noinspection unchecked
    return tuple.get(composeActionParamFactorName(name, i, j));
  }

  Stream<Edge<SUT>> allPossibleEdges(Predicate<State<SUT>> from, Predicate<Action<SUT>> action, Predicate<State<SUT>> to) {
    //noinspection unchecked
    return new StreamableCartesianator<Object>(
        model.states().stream().filter(from).collect(toList()),
        model.actions().stream().filter(action).collect(toList()),
        model.states().stream().filter(to).collect(toList())
    ).stream().flatMap(objects -> allPossibleEdges(
        (State<SUT>) objects.get(0),
        (Action<SUT>) objects.get(1),
        (State<SUT>) objects.get(2)
    ));
  }

  private Stream<Edge<SUT>> allPossibleEdges(State<SUT> from, Action<SUT> action, State<SUT> to) {
    //noinspection unchecked
    return this.model.actions().stream()
        .filter(action::equals)
        .flatMap(
            eachAction -> new StreamableCartesianator<>(actionAndArgsList(eachAction)).stream())
        .map((List<Object> arguments) -> Edge.Builder.from(from)
            .with(
                (Action<SUT>) arguments.get(0),
                new Args(arguments.subList(1, arguments.size()).toArray()))
            .to(to)
            .build())
        .filter(Edge::isValid);
  }

  private List<List<Object>> actionAndArgsList(final Action<SUT> eachAction) {
    return new AbstractList<List<Object>>() {
      @Override
      public int size() {
        return eachAction.numParameterFactors() + 1;
      }

      @Override
      public List<Object> get(int index) {
        return new ArrayList<Object>() {{
          add(eachAction);
          addAll(eachAction.parameters().get(index).getLevels());
        }};
      }
    };
  }
}
