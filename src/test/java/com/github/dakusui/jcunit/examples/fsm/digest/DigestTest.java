package com.github.dakusui.jcunit.examples.fsm.digest;

import com.github.dakusui.jcunit.fsm.Expectation;
import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.fsm.spec.ActionSpec;
import com.github.dakusui.jcunit.fsm.spec.FSMSpec;
import com.github.dakusui.jcunit.fsm.spec.StateSpec;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;


public class DigestTest {
  public static enum Spec implements FSMSpec<Class<MessageDigest>> {
    @StateSpec I {
      @ActionSpec(mandatory=true) Expectation<Class<MessageDigest>> getInstance(FSM<Class<MessageDigest>> fsm) {
        return null;
      }

      public boolean check(Class<MessageDigest> messageDigest) {
        return true;
      }

    },
    @StateSpec(mandatory=true) Instantiated {
      @Override
      public boolean check(Class<MessageDigest> messageDigestClass) {
        return false;
      }
    };

  }

  public static enum MessageDigestSpec implements FSMSpec<MessageDigest> {
    ;

    @Override
    public boolean check(MessageDigest messageDigest) {
      return false;
    }
  }

  public static void main(String[] args) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA1");
    System.out.println(Arrays.toString(md.digest()));
  }
}
