package com.github.dakusui.jcunit8.ututiles;

import com.github.jcunit.core.tuples.Tuple;
import com.github.jcunit.pipeline.PipelineConfig;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public class PipelineConfigBuilder {
  private int strength = 2;
  private boolean negativeTestGeneration;
  private final List<Tuple> seeds = new LinkedList<>();

  public PipelineConfigBuilder() {
  }

  public PipelineConfigBuilder withStrength(int strength) {
    this.strength = strength;
    return this;
  }

  public PipelineConfigBuilder withNegativeTestGeneration(boolean enable) {
    this.negativeTestGeneration = enable;
    return this;
  }

  public PipelineConfigBuilder addSeed(Tuple seed) {
    this.seeds.add(requireNonNull(seed));
    return this;
  }

  public PipelineConfig build() {
    return new PipelineConfig() {
      @Override
      public int strength() {
        return strength;
      }

      @Override
      public boolean generateNegativeTests() {
        return negativeTestGeneration;
      }

      @Override
      public List<Tuple> seeds() {
        return Collections.unmodifiableList(seeds);
      }
    };
  }
}
