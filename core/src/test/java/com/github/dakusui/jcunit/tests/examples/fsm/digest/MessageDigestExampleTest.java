package com.github.dakusui.jcunit.tests.examples.fsm.digest;

import com.github.dakusui.jcunit.examples.fsm.digest.MessageDigestExample;
import com.github.dakusui.jcunit.testutils.Metatest;
import org.junit.Test;

public class MessageDigestExampleTest extends Metatest {
  public MessageDigestExampleTest() {
    super(
        MessageDigestExample.class,
        7 /* expectedRunCount*/,
        0 /* expectedFailureCount */,
        0 /* expectedIgnoreCount */);
  }

  @Test
  public void test() {
    runTests();
  }
}
