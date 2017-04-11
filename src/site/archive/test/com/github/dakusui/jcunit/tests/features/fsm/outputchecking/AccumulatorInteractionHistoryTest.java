package com.github.dakusui.jcunit.tests.features.fsm.outputchecking;

import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.runners.standard.annotations.As;
import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.testutils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class AccumulatorInteractionHistoryTest {
  public enum AccumulatorSpec implements FSMSpec<Accumulator> {
    @StateSpec I,
    ;

    @Override
    public boolean check(Accumulator accumulator) {
      return true;
    }

    @ActionSpec
    public Expectation<Accumulator> add(Expectation.Builder<Accumulator> b, @As("value") int a) {
      return b.valid(I).build();
    }

    @ParametersSpec
    public static final Parameters add = new ParametersBuilder()
    .addParameter().withValues(1, 2, 10)
    .build();

    @ActionSpec
    public Expectation<Accumulator> get(Expectation.Builder<Accumulator> b) {
      return b.valid(I, new OutputChecker.ForInteractionHistory(Output.Type.VALUE_RETURNED) {
        @Override
        protected Object computeExpectation(InteractionHistory interactionHistory) {
          int ret = 0;
          for (Object each : interactionHistory.get("value")) {
            ret += (Integer)each;
          }
          return ret;
        }
      }).build();
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Accumulator, AccumulatorInteractionHistoryTest.AccumulatorSpec> story;

  @Before
  public void before() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void test() {
    FSMUtils.performStory(this, "story", new Accumulator());
  }
}
