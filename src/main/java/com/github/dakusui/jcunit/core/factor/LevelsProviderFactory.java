package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.reflect.Field;
import java.util.List;

public class LevelsProviderFactory {
  public static final LevelsProviderFactory INSTANCE = new LevelsProviderFactory();

  private LevelsProviderFactory() {
  }

  public LevelsProvider createLevelsProvider(Field targetField, FactorField ann,
      List<String> errors) {
    LevelsProvider ret = createLevelsProvider(ann.levelsProvider(), errors);
    ret.setTargetField(targetField);
    ret.setAnnotation(ann);
    ret.init(ann.providerParams());
    errors.addAll(errors);
    return ret;
  }

  private LevelsProvider createLevelsProvider(
      Class<? extends LevelsProvider> levelsProviderClass,
      List<String> errors) {
    LevelsProvider ret = null;
    try {
      ret = ReflectionUtils.create(levelsProviderClass);
    } catch (JCUnitException e) {
      errors.add(Utils.format("Failed to instantiate a class '%s'(%s)",
          levelsProviderClass, e.getMessage()));
    }
    return ret;
  }
}
