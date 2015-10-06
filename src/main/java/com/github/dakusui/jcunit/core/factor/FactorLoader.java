package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Field;

public class FactorLoader {
  private final Factor            factor;
  private final LevelsProvider<?> levelsProvider;

  public FactorLoader(Field f) {
    Checks.checknotnull(f);
    Utils.ValidationResult validationResult = Utils.validateFactorField(f);
    validationResult.check();
    LevelsProvider<?> levelsProvider = validationResult.getLevelsProvider();

    Factor.Builder factorBuilder = new Factor.Builder(f.getName());
    int numLevels = levelsProvider.size();
    for (int i = 0; i < numLevels; i++) {
      factorBuilder.addLevel(levelsProvider.get(i));
    }
    this.factor = factorBuilder.build();
    this.levelsProvider = levelsProvider;
  }

  public Factor getFactor() {
    return this.factor;
  }

  public LevelsProvider<?> getLevelsProvider() {
    return this.levelsProvider;
  }

}
