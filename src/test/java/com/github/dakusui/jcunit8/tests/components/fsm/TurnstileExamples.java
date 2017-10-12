package com.github.dakusui.jcunit8.tests.components.fsm;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.fsm.Edge;
import com.github.dakusui.jcunit8.factorspace.fsm.Player;
import com.github.dakusui.jcunit8.factorspace.fsm.Scenario;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

public class TurnstileExamples {
  @RunWith(JCUnit8.class)
  public static class Normal {
    @ParameterSource
    public Parameter.Factory turnstile() {
      return Parameter.Fsm.Factory.of(TurnstileSpec.class, 1);
    }

    @Test
    public void performScenario(
        @From("turnstile") Scenario<Turnstile> scenario
    ) {
      new Player.Simple<Turnstile>(new Turnstile()) {
        @Override
        public void visit(Edge<Turnstile> edge) {
          System.out.printf("%s %s %s -%s %s%n", edge.from, edge.action, edge.args, edge.isValid() ? ">" : "X", edge.to);
          super.visit(edge);
        }
      }.play(scenario);
    }
  }

  @RunWith(JCUnit8.class)
  public static class Broken {
    @ParameterSource
    public Parameter.Factory turnstile() {
      return Parameter.Fsm.Factory.of(TurnstileSpec.class, 1);
    }

    @Test
    public void performScenario(
        @From("turnstile") Scenario<Turnstile> scenario
    ) {
      new Player.Simple<Turnstile>(new Turnstile.Broken()) {
        @Override
        public void visit(Edge<Turnstile> edge) {
          System.out.printf("%s %s %s -%s %s%n", edge.from, edge.action, edge.args, edge.isValid() ? ">" : "X", edge.to);
          super.visit(edge);
        }
      }.play(scenario);
    }
  }

}
