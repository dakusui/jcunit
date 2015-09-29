package com.github.dakusui.jcunit.examples.fsm.digest;

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

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@RunWith(JCUnit.class)
public class DigestTest {
  public enum Spec implements FSMSpec<Class<MessageDigest>> {
    @StateSpec I {
    };

    @ActionSpec public Expectation<Class<MessageDigest>> getInstance(Expectation.Builder<Class<MessageDigest>> b, String algorithm) {
      return b.valid(I, new Expectation.Checker.FSM("digest")).build();
    }

    @Override
    public boolean check(Class<MessageDigest> messageDigestClass) {
      return true;
    }
  }

  public enum MessageDigestSpec implements FSMSpec<MessageDigest> {
    @StateSpec I {
      @Override
      public Expectation<MessageDigest> digest(Expectation.Builder<MessageDigest> b) {
        return b.invalid().build();
      }

      @Override
      public Expectation<MessageDigest> update(Expectation.Builder<MessageDigest> b, byte[] data) {
        return b.valid(J).build();
      }
    },
    @StateSpec J {
      @Override
      public Expectation<MessageDigest> digest(Expectation.Builder<MessageDigest> b) {
        return b.valid(J, CoreMatchers.instanceOf(new byte[0].getClass())).build();
      }

      @Override
      public Expectation<MessageDigest> update(Expectation.Builder<MessageDigest> b, byte[] data) {
        return b.valid(J, CoreMatchers.anything()).build();
      }
    };

    @ActionSpec
    public abstract Expectation<MessageDigest> digest(Expectation.Builder<MessageDigest> b);

    @ActionSpec
    public abstract Expectation<MessageDigest> update(Expectation.Builder<MessageDigest> b, byte[] data);

    @Override
    public boolean check(MessageDigest digest) {
      return true;
    }
  }

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<Spec, Class<MessageDigest>> factory;

  @FactorField(levelsProvider = FSMLevelsProvider.class)
  public Story<MessageDigestSpec, MessageDigest> digest;

  @Test
  public void test() throws NoSuchAlgorithmException {
    FSMUtils.performStory(this, "factory", MessageDigest.class);
    if (!digest.isPerformed()) {
      FSMUtils.performStory(this, "digest", MessageDigest.getInstance("SHA1"));
    }
  }
}
