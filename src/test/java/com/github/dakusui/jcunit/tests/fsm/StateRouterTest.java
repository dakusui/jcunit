package com.github.dakusui.jcunit.tests.fsm;

import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import org.junit.Test;

public class StateRouterTest {
  public static class Example {
    // This method is reflectively accessed by JCUnit.
    @SuppressWarnings("unused")
    public void action(int p1, int p2) {
    }
  }

  public enum Spec implements FSMSpec<Example> {
    @StateSpec I {
      @Override
      public Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2) {
        if (p1 == 0)
          return b.valid(S0).build();
        return b.invalid().build();
      }
    },
    @StateSpec S0 {
      @Override
      public Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2) {
        if (p1 > 1)
          return b.valid(S1).build();
        return b.invalid().build();
      }
    },
    @StateSpec S1 {
      @Override
      public Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2) {
        return b.invalid().build();
      }
    };

    @ParametersSpec
    public final static Parameters action = new Parameters.Builder()
        .add("p1", 0, 1, 2)
        .add("p2", 0, 1, 2)
        .build();

    @ActionSpec
    public abstract Expectation<Example> action(Expectation.Builder<Example> b, int p1, int p2);

    @Override
    public boolean check(Example example) {
      return true;
    }
  }

  FSM<Example> createFSM() {
    return new FSM.Base<Example>("example", Spec.class, 3);
  }


  @Test
  public void test0() {
    final FSM<Example> fsm = createFSM();
    StateRouter<Example> router = new StateRouter.Base<Example>(fsm);
    State<Example> each = fsm.states().get(0);
    System.out.println("dest=" + each + ":" + router.routeTo(each));
  }

  @Test
  public void test1() {
    final FSM<Example> fsm = createFSM();
    StateRouter<Example> router = new StateRouter.Base<Example>(fsm);
    State<Example> each = fsm.states().get(1);
    System.out.println("dest=" + each + ":" + router.routeTo(each));
  }

  @Test
  public void test2() {
    final FSM<Example> fsm = createFSM();
    StateRouter<Example> router = new StateRouter.Base<Example>(fsm);
    State<Example> each = fsm.states().get(2);
    System.out.println("dest=" + each + ":" + router.routeTo(each));
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Example, Spec> example;
}
