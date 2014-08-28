package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.Utils;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class FactorLoader {
    private final Field field;

    public FactorLoader(Field f) {
        this.field = f;
    }

    private ValidationResult validate(Field f) {
        Checks.checknotnull(f);
        FactorField ann = f.getAnnotation(FactorField.class);
        Checks.checknotnull(ann);
        List<String> errors = new LinkedList<String>();
        LevelsProvider<?> levelsProvider = LevelsProviderFactory.INSTANCE.createLevelsProvider(
                f,
                ann,
                errors
        );
        errors.addAll(levelsProvider.getErrorsOnInitialization());
        ValidationResult ret;
        if (errors.isEmpty()) {
            levelsProvider.setAnnotation(ann);
            levelsProvider.setTargetField(field);
            levelsProvider.init(ParamType.processParams(levelsProvider.parameterTypes(), ann.providerParams()));
            ret = new ValidationResult(true, levelsProvider, null);
        } else {
            ret = new ValidationResult(false, null,
                    Utils.join("; ", errors.toArray()));
        }
        return ret;
    }

    public Factor getFactor() {
        ValidationResult validationResult = this.validate(this.field);
        validationResult.check();
        LevelsProvider<?> levelsProvider = validationResult.getLevelsProvider();

        Factor.Builder factorBuilder = new Factor.Builder();
        factorBuilder.setName(this.field.getName());
        int numLevels = levelsProvider.size();
        for (int i = 0; i < numLevels; i++) {
            factorBuilder.addLevel(levelsProvider.get(i));
        }
        return factorBuilder.build();
    }

    public static class ValidationResult {
        private final boolean valid;
        private final String errMessage;
        private final LevelsProvider<?> levelsProvider;

        public ValidationResult(boolean valid,
                                LevelsProvider<?> levelsProvider, String errorMessage) {
            if (valid) {
                Checks.checknotnull(levelsProvider);
            } else {
                Checks.checknotnull(errorMessage);
            }
            this.valid = valid;
            this.levelsProvider = levelsProvider;
            this.errMessage = errorMessage;
        }

        public LevelsProvider<?> getLevelsProvider() {
            return levelsProvider;
        }

        public void check() {
          Checks.checktest(this.valid, errMessage);
        }
    }
}
