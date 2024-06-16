package com.github.jcunit.pipeline;

import com.github.jcunit.core.tuples.Tuple;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Requirement {
  int strength();

  boolean generateNegativeTests();

  List<Tuple> seeds();

  class Builder {
    private int strength = 2;
    private boolean negativeTestGeneration;
    private final List<Tuple> seeds = new LinkedList<>();

    public Builder() {
    }

    public Builder withStrength(int strength) {
      this.strength = strength;
      return this;
    }

    public Builder withNegativeTestGeneration(boolean enable) {
      this.negativeTestGeneration = enable;
      return this;
    }

    public Builder addSeed(Tuple seed) {
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
        public List<Tuple> seeds() {
          return Collections.unmodifiableList(seeds);
        }
      };
    }
  }
}

