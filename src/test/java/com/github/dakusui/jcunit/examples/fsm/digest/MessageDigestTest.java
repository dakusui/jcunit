package com.github.dakusui.jcunit.examples.fsm.digest;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
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
public class MessageDigestTest {
  @FactorField(stringLevels = {"SHA1", "MD5"})
  public String algotithmName;

  public class Adapter {
    private final MessageDigest messageDigest;

    public Adapter(MessageDigest messageDigest) {
      this.messageDigest = messageDigest;
    }

    public byte[] digest() {
      return this.messageDigest.digest();
    }

    public void update(byte[] input) {
      this.messageDigest.update(input);
    }
  }

  public enum Spec implements FSMSpec<Adapter> {
    @StateSpec I {
      @Override
      public Expectation<Adapter> digest(Expectation.Builder<Adapter> b) {
        return b.valid(I, CoreMatchers.instanceOf(new byte[0].getClass())).build();
      }

      @Override
      public Expectation<Adapter> update(Expectation.Builder<Adapter> b, byte[] data) {
        return b.valid(I).build();
      }
    },;

    @ActionSpec
    public abstract Expectation<Adapter> digest(Expectation.Builder<Adapter> b);

    @ParametersSpec
    public static final Object[][] update = new Object[][] {
        new Object[] {
            new byte[] { 0 }
        }
    };

    @ActionSpec
    public abstract Expectation<Adapter> update(Expectation.Builder<Adapter> b, byte[] data);

    @Override
    public boolean check(Adapter messageDigest) {
      return true;
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Spec, Adapter> messageDigest;

  @Test
  public void test() throws NoSuchAlgorithmException {
    Adapter md = new Adapter(MessageDigest.getInstance(this.algotithmName));
    FSMUtils.performStory(this, "messageDigest", md);
  }
}
