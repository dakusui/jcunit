package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.fsm.Action;
import com.github.dakusui.jcunit.fsm.Args;
import com.github.dakusui.jcunit.fsm.Output;
import com.github.dakusui.jcunit.fsm.State;

public class Edge<SUT> implements Stimulus<SUT> {
  public final State<SUT>  from;
  public final Action<SUT> action;
  public final Args        args;
  public final State<SUT>  to;

  Edge(State<SUT> from, Action<SUT> action, Args args, State<SUT> to) {
    this.from = from;
    this.action = action;
    this.args = args;
    this.to = to;
  }

  public boolean isPossible() {
    return action.parameters().size() == args.size() &&
        from.expectation(action, args).state.equals(to);
  }

  public boolean isValid() {
    return from.expectation(action, args).getType() == Output.Type.VALUE_RETURNED;
  }

  @Override
  public void accept(Player<SUT> player) {
    player.visit(this);
  }

  @Override
  public String toString() {
    return String.format("%s.%s(%s)->%s", this.from, this.action, this.args, this.to);
  }

  public static class Builder<SUT> {
    private final State<SUT>  from;
    private       Action<SUT> action;
    private       Args        args;
    private       State<SUT>  to;

    Builder(State<SUT> from) {
      this.from = from;
    }

    public static <SUT> Builder<SUT> from(State<SUT> from) {
      return new Builder<>(from);
    }

    public Builder<SUT> with(Action<SUT> action, Args args) {
      this.action = action;
      this.args = args;
      return this;
    }

    public Builder<SUT> to(State<SUT> to) {
      this.to = to;
      return this;
    }

    public Edge<SUT> build() {
      assert action.numParameterFactors() == args.size();
      if (!to.equals(from.expectation(action, args).state)) {
        throw new RuntimeException();
      }
      return new Edge<>(from, action, args, to);
    }
  }
}
