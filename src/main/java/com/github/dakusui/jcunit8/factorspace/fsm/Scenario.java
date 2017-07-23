package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.exceptions.InvalidTestException;
import com.github.dakusui.jcunit.fsm.Action;
import com.github.dakusui.jcunit.fsm.Args;
import com.github.dakusui.jcunit.fsm.FiniteStateMachine;
import com.github.dakusui.jcunit.fsm.State;
import com.github.dakusui.jcunit.fsm.spec.FsmSpec;

import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.util.stream.Collectors.toList;

public interface Scenario<SUT> extends Stimulus<SUT> {
  Sequence<SUT> setUp();

  Sequence<SUT> main();

  static <SUT, SPEC extends Enum & FsmSpec<SUT>> Scenario.Builder<SUT, SPEC> builder(String name, Class<SPEC> specClass) {
    return new Builder<>(name, specClass);
  }

  String name();

  class Impl<SUT> implements Scenario<SUT> {
    private final Sequence<SUT> setUp;
    private final Sequence<SUT> main;
    private final String        name;

    public Impl(String name, Sequence<SUT> setUp, Sequence<SUT> main) {
      this.setUp = setUp;
      this.main = main;
      this.name = Objects.requireNonNull(name);
    }

    @Override
    public Sequence<SUT> setUp() {
      return this.setUp;
    }

    @Override
    public Sequence<SUT> main() {
      return this.main;
    }

    @Override
    public String name() {
      return this.name;
    }

    @Override
    public void accept(Player<SUT> player) {
      player.visit(this);
    }

    @Override
    public String toString() {
      return String.format("setUp:%s; main:%s", setUp, main);
    }
  }

  class Builder<SUT, SPEC extends Enum & FsmSpec<SUT>> {
    private final Class<SPEC>             spec;
    private final FiniteStateMachine<SUT> fsm;
    private final String                  name;
    private       SPEC                    startFrom;
    private final List<Edge<SUT>> edges = new LinkedList<>();

    public Builder(String name, Class<SPEC> spec) {
      this.name = name;
      this.spec = Objects.requireNonNull(spec);
      this.fsm = FiniteStateMachine.create(name, spec);
      this.startFrom(this.fsm.initialState().spec());
    }

    public Builder<SUT, SPEC> startFrom(SPEC state) {
      this.startFrom = Objects.requireNonNull(state);
      return this;
    }

    public Builder<SUT, SPEC> doAction(String name, Object... args) {
      Objects.requireNonNull(name);
      return this.doAction(
          findAction(name),
          new Args(args)
      );
    }

    public Scenario<SUT> build() {
      Sequence<SUT> setup = new FsmComposer<>(
          this.name,
          this.fsm,
          -1
      ).composeScenarioToBringUpFsmTo(
          findState(
              this.startFrom
          )
      );
      //noinspection Convert2MethodRef
      Sequence<SUT> main = this.edges.stream().collect(
          () -> new Sequence.Builder<>(),
          Sequence.Builder::add,
          (Sequence.Builder<SUT> builder1, Sequence.Builder<SUT> builder2) -> {
            builder1.addAll(builder2.edges);
            builder2.addAll(builder1.edges);
          }
      ).build();
      return new Impl<>(this.name, setup, main);
    }

    private Action<SUT> findAction(String name) {
      return this.fsm.actions().stream(
      ).filter(
          action -> action.id().startsWith(name)
      ).findFirst(
      ).orElseThrow(
          () -> new InvalidTestException(
              String.format(
                  "No action matching '%s' was found in '%s': [%s]",
                  name,
                  this.spec.getCanonicalName(),
                  this.fsm.actions().stream(
                  ).map(
                      Action::id
                  ).collect(
                      toList()
                  )
              )
          )
      );
    }

    private Builder<SUT, SPEC> doAction(Action<SUT> action, Args args) {
      this.edges.add(new Edge.Builder<>(currentState())
          .with(action, args)
          .to(currentState().expectation(action, args).state)
          .build());
      return this;
    }

    private State<SUT> findState(SPEC stateSpec) {
      return this.fsm.states().stream(
      ).filter(
          eachState -> eachState.spec().equals(stateSpec)
      ).findFirst(
      ).orElseThrow(
          () -> new InvalidTestException(
              String.format(
                  "No state matching '%s' was found in '%s': [%s]",
                  stateSpec,
                  this.spec.getCanonicalName(),
                  this.fsm.states().stream(
                  ).map(
                      eachState -> eachState == this.fsm.initialState() ?
                          String.format("*%s*", eachState.spec()) :
                          eachState.spec().toString()
                  ).collect(
                      toList()
                  )
              )
          )
      );
    }

    private State<SUT> currentState() {
      return this.edges.isEmpty() ?
          findState(this.startFrom) :
          this.edges.get(this.edges.size() - 1).to;
    }
  }
}
