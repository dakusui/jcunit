package com.github.dakusui.jcunit.examples.fsm.param;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.UndefinedSymbol;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.junit.Test;
import org.junit.runner.RunWith;

import static com.github.dakusui.jcunit.fsm.SUTFactory.Base.$;

@RunWith(JCUnit.class)
public class FSMParamTest {
  public static class Sut {
    String s;
    int    i;

    public Sut(String s, int i) {
      this.s = s;
      this.i = i;
    }

    @SuppressWarnings("unused")
    public void method(String s, int i) {
      this.s = this.s + s + ":";
      this.i = this.i + i;
    }

    public int getI() {
      return this.i;
    }

    public String getS() {
      return this.s;
    }
  }

  public enum Spec implements FSMSpec<Sut> {
    @StateSpec I {
    };

    @ParametersSpec
    public static final Parameters method = new Parameters.Builder(new Object[][] {
        { "A", "B", "C" },
        { 1, 2, 3 }
    }).build();

    @ActionSpec
    public Expectation<Sut> method(Expectation.Builder<Sut> b, String s, int i) {
      return b.valid(I)
          .addCollector(new InputHistory.Collector.Default("method"))
          .build();
    }

    @ActionSpec
    public Expectation<Sut> getS(Expectation.Builder<Sut> b) {
      return b.valid(
          I,
          new OutputChecker.ForInputHistory(Output.Type.VALUE_RETURNED) {
            @Override
            protected Object computeExpectation(InputHistory inputHistory) throws UndefinedSymbol {
              return "prefix:" + inputHistory.get("method@param-0").iterator().next() + ":";
            }
          }
      ).build();
    }

    @ActionSpec
    public Expectation<Sut> getI(Expectation.Builder<Sut> b) {
      return b.valid(I).build();
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
    FSMUtils.performStory(
        this,
        "paramstory",
        new SUTFactory.Simple<Sut>(
            Sut.class,
            $(String.class, "prefix:"),
            $(Integer.TYPE, 1)
        ) {
        });
  }
}
