package com.github.dakusui.jcunit.examples.dfsm;

import com.github.dakusui.jcunit.core.FactorField;

public class DFSMTest {
  public Turnstile sut = new Turnstile();
  @FactorField
  public Machine.Transition t0;

  @FactorField
  public Machine.Transition t1;

  @FactorField
  public Machine.Transition t2;


  public static class Machine {
    public static class Expectation {
      State     s;
      Object    result;
      Throwable t;
    }

    public static class Transition {
      State s;
      Symbol input;
      Expectation expectation;
    }
    public static enum State {
      s0 {
        @Override
        public State input(Symbol e) {
          return s0;
        }
      };

      public State input(Symbol e) {
        throw new IllegalStateException();
      };
    }

    public static enum Symbol {
      s1,
      s2;
    }

    public void perform(Transition t) {
      State s$ = t.s.input(t.input);
    }
  }
}
