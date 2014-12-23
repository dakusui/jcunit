package com.github.dakusui.jcunit.fsm.sut;

import com.github.dakusui.jcunit.constraint.ConstraintManager;
import com.github.dakusui.jcunit.constraint.constraintmanagers.ConstraintManagerBase;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.fsm.Action;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.FSMTupleGenerator;
import com.github.dakusui.jcunit.fsm.State;
import com.github.dakusui.jcunit.fsm.example.FlyingSpaghettiMonster;
import org.junit.runner.RunWith;


@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = FlyingSpaghettiMonsterTest.FSMFSM.class,

        params = {
            @Param("com.github.dakusui.jcunit.core.TupleGeneration"),
            @Param("2")
        }
    ),
    constraint = @Constraint(
        FlyingSpaghettiMonsterTest.CM.class
    )
)
public class FlyingSpaghettiMonsterTest {
  public static class FSMFSM extends FSMTupleGenerator<FlyingSpaghettiMonster> {

    @Override
    protected com.github.dakusui.jcunit.fsm.FSM<FlyingSpaghettiMonster> createFSM() {
      return new FSM<FlyingSpaghettiMonster>() {

        @Override
        public State<FlyingSpaghettiMonster> initialState() {
          return this.states()[0];
        }

        @Override
        public State<FlyingSpaghettiMonster>[] states() {
          return null;
        }

        @Override
        public Action<FlyingSpaghettiMonster>[] actions() {
          return null;
        }
      };
    }
  }

  public static class CM extends ConstraintManagerBase {

    @Override
    public boolean check(Tuple tuple) throws UndefinedSymbol {
      return true;
    }
  }
}
