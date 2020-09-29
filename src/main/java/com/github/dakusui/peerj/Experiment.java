package com.github.dakusui.peerj;

public interface Experiment {
  Report conduct();

  interface Report {
  }
}
