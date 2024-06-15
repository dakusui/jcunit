package com.github.jcunit.factorspace;

import java.util.List;

import static java.util.Arrays.asList;

public interface Factor {
  Object VOID     = new VoidValue();

  class VoidValue {
    private VoidValue() {
    }

    @Override
    public String toString() {
      return "(VOID)";
    }
  }
  
  static Factor create(final String name, final Object[] args) {
    return new Factor() {
      private final List<Object> levels = asList(args);

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
