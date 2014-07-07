package com.github.dakusui.jcunit.tutorial.session02;

import com.github.dakusui.jcunit.compat.core.annotations.Out.Verifier;

public class CustomVerifier implements Verifier {

  @Override
  public boolean verify(Object expected, Object actual) {
    if (expected == null) {
      return actual == null;
    }
    if (expected instanceof Exception) {
      String emsg = ((Exception) expected).getMessage();
      if (actual == null) {
        return false;
      }
      String amsg = ((Exception) actual).getMessage();
      if (emsg == null) {
        return amsg == null;
      }
      return emsg.equals(amsg);
    }
    throw new RuntimeException("This verifier is only for Exception objects.");
  }

}
