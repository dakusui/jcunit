package com.github.dakusui.jcunit.examples.fsm.param;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.$;
import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.INT_ARRAY_TYPE;

@RunWith(JCUnit.class)
public class FSMParamTest {
  public static class Sut {
    boolean init = false;
    String s;

    public Sut(
        String s,
        // This parameter is only for illustration of how to call a constructor with parameters
        @SuppressWarnings("unused")int i,
        // This parameter is only for illustration of how to call a constructor with parameters
        @SuppressWarnings("unused")int[] j
    ) {
      this.s = s + ":";
    }

    @SuppressWarnings("unused")
    public void method(String s) {
      init = true;
      this.s = this.s + s + ":";
    }

    public String getS() {
      if (!init)
        throw new IllegalStateException();
      return this.s;
    }
  }

  public enum Spec implements FSMSpec<Sut> {
    @StateSpec I {
    },
    @StateSpec S {
      @ActionSpec
      public Expectation<Sut> getS(Expectation.Builder<Sut> b) {
        return b.valid(
            S,
            new OutputChecker.ForInputHistory(Output.Type.VALUE_RETURNED) {
              @Override
              protected Object computeExpectation(InputHistory inputHistory) throws UndefinedSymbol {
                return compose(inputHistory.get("method@param-0"));
              }

              private String compose(Iterable<Object> i) {
                StringBuilder b = new StringBuilder();
                for (Object each : i) {
                  b.append(each);
                  b.append(":");
                }
                return b.toString();
              }
            }
        ).build();
      }

      @ActionSpec
      public Expectation<Sut> getI(Expectation.Builder<Sut> b) {
        return b.valid(S).build();
      }
    };

    @ParametersSpec
    public static final Parameters method = new Parameters.Builder(new Object[][] {
        { "A", "B", "C" }
    }).build();

    @ActionSpec
    public Expectation<Sut> method(Expectation.Builder<Sut> b, String s) {
      return b.valid(S)
          .addCollector(new InputHistory.Collector.Default("method"))
          .build();
    }

    @ActionSpec
    public Expectation<Sut> getS(Expectation.Builder<Sut> b) {
      return b.invalid().build();
    }

    @Override
    public boolean check(Sut sut) {
      return true;
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Sut, Spec> paramstory;

  @Test
  public void test() {
    int[] ii = new int[0];
    FSMUtils.performStory(
        this,
        "paramstory",
        ////
        // Illustrating how to access constructor with various types of parameters.
        new SUTFactory.Simple<Sut>(
            Sut.class,
            $(String.class, "prefix"),
            $(Integer.TYPE, 1),
            $(INT_ARRAY_TYPE, ii)
        ).addCollector(new InputHistory.Collector.Default("method")),
        ScenarioSequence.Observer.Factory.ForSilent.INSTANCE
    );
  }
}
