package com.github.dakusui.jcunit8.factorspace;

import java.util.List;

import static java.util.Arrays.asList;

public interface Factor {
  static Factor create(final String name, final Object[] args) {
    return new Factor() {
      private List<Object> levels = asList(args);

      @Override
      public String getName() {
        return name;
      }

      @Override
      public List<Object> getLevels() {
        return levels;
      }

      @Override
      public String toString() {
        return String.format("%s:%s", name, levels);
      }
    };
  }

  String getName();

  List<Object> getLevels();
}
