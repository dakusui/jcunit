package com.github.dakusui.jcunit8.factorspace.fsm;

import com.github.dakusui.jcunit.fsm.Action;
import com.github.dakusui.jcunit.fsm.Args;
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

  public boolean isValid() {
    return action.parameters().size() == args.size() &&
        from.expectation(action, args).state.equals(to);
  }

  @Override
  public void accept(Player<SUT> player) {
    player.play(this);
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
      assert to.equals(from.expectation(action, args));
      return new Edge<>(from, action, args, to);
    }
  }
}
