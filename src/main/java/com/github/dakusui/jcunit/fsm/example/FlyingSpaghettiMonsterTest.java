package com.github.dakusui.jcunit.fsm.example;

import com.github.dakusui.jcunit.core.Generator;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.TupleGeneration;
import com.github.dakusui.jcunit.fsm.*;
import org.hamcrest.CoreMatchers;
import org.junit.runner.RunWith;
import sun.plugin.dom.exception.InvalidStateException;

@RunWith(JCUnit.class)
@TupleGeneration(
    generator = @Generator(
        value = FlyingSpaghettiMonsterTest.Generator.class
    ))
public class FlyingSpaghettiMonsterTest {
  public static class FSMFSM implements FSM<FlyingSpaghettiMonster> {
    public enum S implements State<FlyingSpaghettiMonster> {
      I {
        @Override public Expectation<FlyingSpaghettiMonster> expectation(
            Action action, Args args) {
          if (action.equals(D.COOK)) return new Expectation<FlyingSpaghettiMonster>(FULL, CoreMatchers.anything());
          return super.expectation(action, args);
        }

        @Override public boolean matches(
            FlyingSpaghettiMonster flyingSpaghettiMonster) {
          return !flyingSpaghettiMonster.isReady();
        }
      },
      FULL {
        @Override public Expectation<FlyingSpaghettiMonster> expectation(
            Action action, Args args) {
          if (action.equals(D.EAT)) new Expectation<FlyingSpaghettiMonster>(I, CoreMatchers.anything());
          return super.expectation(action, args);
        }

        @Override public boolean matches(
            FlyingSpaghettiMonster flyingSpaghettiMonster) {
          return !flyingSpaghettiMonster.isReady();
        }
      };

      @Override public Expectation expectation(Action action, Args args) {
        throw new InvalidStateException(String
            .format("'%s(%s)' can't be performed on state '%s'.", action, args,
                this));
      }
    }

    public static enum D implements Action<FlyingSpaghettiMonster> {
      COOK {
        @Override public Object perform(
            FlyingSpaghettiMonster flyingSpaghettiMonster,
            Args args) throws Throwable {
          return flyingSpaghettiMonster.cook((String)args.values()[0]);
        }

        @Override public Object[] param(int i) {
          return new Object[]{
              "spaghetti",
              "soba"
          };
        }

        @Override public int numParams() {
          return 1;
        }
      },
      EAT {
        @Override public Object perform(
            FlyingSpaghettiMonster flyingSpaghettiMonster,
            Args args) throws Throwable {
          return flyingSpaghettiMonster.eat();
        }

        @Override public Object[] param(int i) {
          return new Object[0];
        }

        @Override public int numParams() {
          return 0;
        }
      };
    }

    @Override public S initialState() {
      return S.I;
    }

    @Override public S[] states() {
      return S.values();
    }

    @Override public D[] actions() {
      return D.values();
    }
  }

  public static class Generator
      extends ScenarioTupleGenerator<FlyingSpaghettiMonster> {
    @Override protected FSM<FlyingSpaghettiMonster> createFSM() {
      return new FSMFSM();
    }
  }
}
