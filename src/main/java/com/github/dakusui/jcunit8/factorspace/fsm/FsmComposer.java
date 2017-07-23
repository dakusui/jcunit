package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.fsm.FiniteStateMachine;
import com.github.dakusui.jcunit.fsm.State;

import java.util.*;
import java.util.stream.IntStream;

import static com.github.dakusui.jcunit8.exceptions.TestDefinitionException.fsmDoesNotHaveRouteToSpecifiedState;
import static java.util.stream.Collectors.toList;

public class FsmComposer<SUT> extends FsmTupleAccessor<SUT> {

  public FsmComposer(String name, FiniteStateMachine<SUT> model, int scenarioLength) {
    super(name, model, scenarioLength);
  }

  public Scenario<SUT> composeValueFrom(Tuple tuple) {
    Sequence<SUT> main = composeScenarioFromTuple(tuple);
    return new Scenario.Impl<>(
        this.name,
        composeScenarioToBringUpFsmTo(main.get(0).from),
        main
    );
  }

  Sequence<SUT> composeScenarioToBringUpFsmTo(State<SUT> destination) {
    if (Objects.equals(this.model.initialState(), destination)) {
      return new Sequence.Builder<SUT>().build();
    }
    return new Sequence.Builder<SUT>().addAll(
        findRoute(
            this.model.initialState(),
            destination,
            new LinkedList<>(),
            new ArrayList<>(this.model.states())
        )
    ).build();
  }

  private List<Edge<SUT>> findRoute(State<SUT> from, State<SUT> to, List<Edge<SUT>> work, List<State<SUT>> notVisited) {
    Optional<Edge<SUT>> edgeOptional = allPossibleEdges(from::equals, sutAction -> true, to::equals).findFirst();
    if (edgeOptional.isPresent()) {
      work.add(edgeOptional.get());
      return work;
    } else {
      for (State<SUT> eachState : notVisited) {
        Optional<Edge<SUT>> cur = allPossibleEdges(from::equals, sutAction -> true, eachState::equals).findFirst();
        if (cur.isPresent()) {
          return findRoute(
              eachState,
              to,
              new ArrayList<Edge<SUT>>(work) {{
                add(cur.get());
              }},
              new ArrayList<State<SUT>>(notVisited) {{
                remove(eachState);
              }});
        }
      }
    }
    throw fsmDoesNotHaveRouteToSpecifiedState(to, this.name, this.model);
  }

  private Sequence<SUT> composeScenarioFromTuple(Tuple tuple) {
    return new Sequence.Builder<SUT>()
        .addAll(
            IntStream.range(0, scenarioLength)
                .mapToObj(i -> Edge.Builder.from(getStateFromTuple(tuple, i))
                    .with(getActionFromTuple(tuple, i), getActionArgsFromTuple(tuple, i))
                    .to(getStateFromTuple(tuple, i)
                        .expectation(
                            getActionFromTuple(tuple, i),
                            getActionArgsFromTuple(tuple, i)
                        ).state)
                    .build())
                .collect(toList())
        )
        .build();
  }
}
