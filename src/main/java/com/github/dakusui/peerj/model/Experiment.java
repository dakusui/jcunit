package com.github.dakusui.peerj.model;

public interface Experiment {
  Report conduct();

  interface Report {
  }
}
