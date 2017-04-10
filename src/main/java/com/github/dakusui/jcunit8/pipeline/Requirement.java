package com.github.dakusui.jcunit8.pipeline;

public interface Requirement {
  int strength();

  boolean generateNegativeTests();

  class Builder {
    private int strength = 2;
    private boolean negativeTestGeneration;

    public Builder withStrength(int strength) {
      this.strength = strength;
      return this;
    }

    public Builder withNegativeTestGeneration(boolean enable) {
      this.negativeTestGeneration = enable;
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
      };
    }
  }
}

