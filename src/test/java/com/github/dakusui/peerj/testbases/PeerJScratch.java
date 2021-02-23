package com.github.dakusui.peerj.testbases;

import com.github.dakusui.jcunit8.core.Utils;
import org.junit.Before;

import static java.lang.String.format;

public abstract class PeerJScratch extends PeerJBase {

  public PeerJScratch(Spec spec) {
    super(spec);
  }

  @Before
  public void before() {
    Utils.invalidateMemos();
  }
}
