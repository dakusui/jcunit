package com.github.dakusui.jcunit8.pipeline;

import com.github.dakusui.jcunit.core.tuples.Aarray;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import static java.util.Objects.requireNonNull;

public interface Requirement {
  int strength();

  boolean generateNegativeTests();

  List<Aarray> seeds();

  class Builder {
    private int strength = 2;
    private boolean            negativeTestGeneration;
    private final List<Aarray> seeds = new LinkedList<>();

    public Builder withStrength(int strength) {
      this.strength = strength;
      return this;
    }

    public Builder withNegativeTestGeneration(boolean enable) {
      this.negativeTestGeneration = enable;
      return this;
    }

    public Builder addSeed(Aarray seed) {
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
        public List<Aarray> seeds() {
          return Collections.unmodifiableList(seeds);
        }
      };
    }
  }
}

