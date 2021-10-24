package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.KeyValuePairs;
import com.github.dakusui.jcunit.core.tuples.Row;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Requirement {
  int strength();

  boolean generateNegativeTests();

  List<Row> seeds();

  class Builder {
    private int strength = 2;
    private boolean                   negativeTestGeneration;
    private final List<Row> seeds = new LinkedList<>();

    public Builder withStrength(int strength) {
      this.strength = strength;
      return this;
    }

    public Builder withNegativeTestGeneration(boolean enable) {
      this.negativeTestGeneration = enable;
      return this;
    }

    public Builder addSeed(Row seed) {
      this.seeds.add(requireNonNull(seed));
      return this;
    }

    public Requirement build() {
      return new Requirement() {
        @Override
        public int strength() {
          return strength;
        }

        @Override
        public boolean generateNegativeTests() {
          return negativeTestGeneration;
        }

        @Override
        public List<Row> seeds() {
          return Collections.unmodifiableList(seeds);
        }
      };
    }
  }
}

