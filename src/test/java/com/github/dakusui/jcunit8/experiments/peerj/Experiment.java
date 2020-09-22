package com.github.dakusui.jcunit8.experiments.peerj;

public interface Experiment {
  Report conduct();

  interface Report {
  }
}
