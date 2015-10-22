package com.github.dakusui.jcunit.examples.fsm.methodoverloading;

import com.github.dakusui.jcunit.annotations.FactorField;
import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class MethodOverloadingTest {
  public static ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;

  public class MethodOverloading {
    public String m() {
      return "m()";
    }
    public String m(String s) {
      return "m(s)";
    }
    public String m_(String s, int i) {
      return "m(s,i)";
    }
    public String m_(int i) {
      return "m(i)";
    }
    public String m_(Object o) {
      return "m(o)";
    }
  }
  public enum MethodOverloadingSpec implements FSMSpec<MethodOverloading> {
    @StateSpec I {

    };
    @ParametersSpec
    public static final Parameters m$s = new Parameters.Builder()
        .add("a", "hello")
        .build();

    @ActionSpec(parametersSpec = "m$s")
    public Expectation<MethodOverloading> m(Expectation.Builder<MethodOverloading> b, String s) {
      return b.valid(I, CoreMatchers.equalTo("m(s)")).build();
    }

    @ActionSpec
    public Expectation<MethodOverloading> m(Expectation.Builder<MethodOverloading> b) {
      return b.valid(I, CoreMatchers.equalTo("m()")).build();
    }

    @Override
    public boolean check(MethodOverloading methodOverloading) {
      return true;
    }
  }


  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<MethodOverloading, MethodOverloadingSpec> primary;

  @Test
  public void test() {
    FSMUtils.performStory(this, "primary", new MethodOverloading(), observerFactory);
  }
}
