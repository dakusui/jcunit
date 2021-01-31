package com.github.dakusui.jcunit8.pipeline.stages.generators.ext.acts;

import com.github.dakusui.peerj.model.Experiment;

public class ActsReport implements Experiment.Report {
  private final int  size;
  private final long generationTime;
  private final int  strength;
  private final int  order;
  private final int  degree;

  public ActsReport(int size, long generationTime, int strength, int order, int degree) {
    this.size = size;
    this.generationTime = generationTime;
    this.strength = strength;
    this.order = order;
    this.degree = degree;
  }

  @Override
  public String toString() {
    return String.format("|CA(%s, %s^%s)|=%s, %s[msec]", strength, order, degree, size, generationTime);
  }
}
