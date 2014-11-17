package com.github.dakusui.jcunit.fsm;

import com.github.dakusui.jcunit.core.factor.Factor;

import java.util.Arrays;
import java.util.List;

public class Param extends Factor {
  public Param(Holder holder, int id, List<Object> levels) {
    super(formatName(holder, id), levels);
  }

  private static String formatName(Holder holder, int id) {
    return String.format("param:%s:%d", holder.name(), id);
  }

  public static interface Holder {
    String name();
  }

  public static interface Factory {
    public static Factory INSTANCE = new Factory() {
      @Override public Param create(Holder holder, Object... levels) {
        int i = 0;
        return new Param(holder, i, Arrays.asList(levels));
      }
    };

    Param create(Holder holder, Object... levels);
  }
}
