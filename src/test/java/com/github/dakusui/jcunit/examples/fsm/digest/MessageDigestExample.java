package com.github.dakusui.jcunit.examples.fsm.digest;

import com.github.dakusui.jcunit.runners.standard.annotations.FactorField;
import com.github.dakusui.jcunit.runners.standard.JCUnit;
import com.github.dakusui.jcunit.fsm.*;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.ParametersSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;
import org.hamcrest.CoreMatchers;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RunWith(JCUnit.class)
public class MessageDigestExample {
  @FactorField(stringLevels = { "SHA1", "MD5" })
  public String algotithmName;

  public enum Spec implements FSMSpec<MessageDigest> {
    @StateSpec I {
      @Override
      public Expectation<MessageDigest> digest(Expectation.Builder<MessageDigest> b) {
        return b.valid(I, CoreMatchers.instanceOf(new byte[0].getClass())).build();
      }

      @Override
      public Expectation<MessageDigest> update(Expectation.Builder<MessageDigest> b, byte[] data) {
        return b.valid(I).build();
      }
    },;

    @ActionSpec
    public abstract Expectation<MessageDigest> digest(Expectation.Builder<MessageDigest> b);

    @ParametersSpec
    public static final Parameters update = new Parameters.Builder(
        new Object[][] {
            new Object[] {
                new byte[] { 0 }
            }
        }
    ).build();

    @ActionSpec(parametersSpec = "update")
    public abstract Expectation<MessageDigest> update(Expectation.Builder<MessageDigest> b, byte[] data);

    @Override
    public boolean check(MessageDigest messageDigest) {
      return true;
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<MessageDigest, Spec> messageDigest;

  @Test
  public void test() throws NoSuchAlgorithmException {
    ScenarioSequence.Observer.Factory observerFactory = ScenarioSequence.Observer.Factory.ForSilent.INSTANCE;
    MessageDigest md = MessageDigest.getInstance(this.algotithmName);
    FSMUtils.performStory(this, "messageDigest", md, observerFactory);
  }
}
