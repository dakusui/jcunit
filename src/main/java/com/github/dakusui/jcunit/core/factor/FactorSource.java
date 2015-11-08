package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.fsm.FSM;
import com.github.dakusui.jcunit.plugins.constraints.Constraint;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;

import java.util.Collections;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checkcond;
import static com.github.dakusui.jcunit.core.Checks.checknotnull;

public abstract class FactorSource {
  public final String name;

  public FactorSource(String name) {
    this.name = checknotnull(name);
  }

  public abstract List<Factor> createFactors();

  public static class Simple extends FactorSource {
    private final LevelsProvider levelsProvider;

    public Simple(String name, LevelsProvider levelsProvider) {
      super(name);
      this.levelsProvider = checknotnull(levelsProvider);
    }

    @Override
    public List<Factor> createFactors() {
      Factor.Builder b = new Factor.Builder(this.name);
      for (int i = 0; i < this.levelsProvider.size(); i++) {
        b.addLevel(this.levelsProvider.get(0));
      }
      return Collections.singletonList(b.build());
    }
  }

  public static class Structured extends FactorSource {
    public final Constraint constraint;

    public Structured(String name, Constraint constraint) {
      super(name);
      this.constraint = checknotnull(constraint);
    }

    @Override
    public List<Factor> createFactors() {
      return null;
    }
  }
  public static class Fsm extends Structured {

    private final FSM<?> fsm;
    private final int historyLength;

    public Fsm(String name, Constraint baseConstraint, FSM<?> fsm, int historyLength) {
      super(name, baseConstraint);
      this.fsm = checknotnull(fsm);
      checkcond(historyLength > 0);
      this.historyLength = historyLength;
    }

    @Override
    public List<Factor> createFactors() {
      return null;
    }
  }
}
