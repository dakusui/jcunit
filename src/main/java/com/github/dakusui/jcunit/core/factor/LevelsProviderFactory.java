package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.FactorField;

import java.lang.reflect.Field;
import java.util.List;

public class LevelsProviderFactory {
    public static final LevelsProviderFactory INSTANCE = new LevelsProviderFactory();

    private LevelsProviderFactory() {
    }

    public LevelsProvider createLevelsProvider(Field targetField, FactorField ann, List<String> errors) {
        LevelsProvider ret = createLevelsProvider(ann.levelsFactory(), errors);
        ret.setTargetField(targetField);
        ret.setAnnotation(ann);
        ret.init(ann.providerParams());
        errors.addAll(errors);
        return ret;
    }

    private LevelsProvider createLevelsProvider(Class<? extends LevelsProvider> levelsProviderClass, List<String> errors) {
        LevelsProvider ret = null;
        try {
            ret = levelsProviderClass.newInstance();
        } catch (InstantiationException e) {
            errors.add(String.format("Failed to instantiate a class '%s'(%s)", levelsProviderClass.getCanonicalName(), e.getMessage()));
        } catch (IllegalAccessException e) {
            errors.add(String.format("Failed to instantiate a class '%s'(%s)", levelsProviderClass.getCanonicalName(), e.getMessage()));
        }
        return ret;
    }
}
