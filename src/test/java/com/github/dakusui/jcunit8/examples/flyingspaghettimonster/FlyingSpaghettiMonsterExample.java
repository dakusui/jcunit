package com.github.dakusui.jcunit8.examples.flyingspaghettimonster;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.factorspace.Parameter.Fsm;
import com.github.dakusui.jcunit8.factorspace.fsm.Edge;
import com.github.dakusui.jcunit8.factorspace.fsm.Player;
import com.github.dakusui.jcunit8.factorspace.fsm.Scenario;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.Condition;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit8.class)
public class FlyingSpaghettiMonsterExample {
  @Condition(constraint = true)
  public boolean check(@From("flyingSpaghettiMonster") Scenario<FlyingSpaghettiMonster> fsm) {
    return true;
  }
  @ParameterSource
  public Parameter.Factory flyingSpaghettiMonster() {
    return Fsm.Factory.of(FlyingSpaghettiMonsterSpec.class, 1);
  }

  @Test
  public void performScenario(
      @From("flyingSpaghettiMonster") Scenario<FlyingSpaghettiMonster> scenario
  ) {
    System.out.println("setup:" + scenario.setUp());
    System.out.println("main:" + scenario.main());
    new Player.Simple<FlyingSpaghettiMonster>(new FlyingSpaghettiMonster()) {
      @Override
      public void visit(Edge<FlyingSpaghettiMonster> edge) {
        System.out.printf("%s %s %s -%s %s%n", edge.from, edge.action, edge.args, edge.isValid() ? ">" : "X", edge.to);
        super.visit(edge);
      }

    }.play(scenario);
  }
}
