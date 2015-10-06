package com.github.dakusui.jcunit.examples.fsm.brokenfsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.io.FileNotFoundException;

@RunWith(JCUnit.class)
public class BrokenFSMTest {
  public enum Spec implements FSMSpec<BrokenFSM> {
    @StateSpec I;

    @ActionSpec public Expectation<BrokenFSM> shouldReturnTrueButThrowsException(Expectation.Builder<BrokenFSM> b) {
      return b.valid(I, CoreMatchers.is(true)).build();
    }

    @ActionSpec public Expectation<BrokenFSM> shouldReturnTrueButReturnsFalse(Expectation.Builder<BrokenFSM> b) {
      return b.valid(I, CoreMatchers.is(true)).build();
    }

    @ActionSpec public Expectation<BrokenFSM> shouldThrowsExceptionButReturnsTrue(Expectation.Builder<BrokenFSM> b) {
      return b.invalid(I, FileNotFoundException.class).build();
    }

    @Override
    public boolean check(BrokenFSM brokenFSM) {
      return true;
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<BrokenFSM, Spec> brokenFSM;

  @Test
  public void test() {
    FSMUtils.performStory(this, "brokenFSM", new BrokenFSM());
  }
}
