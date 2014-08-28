package com.github.dakusui.jcunit.core.factor;

import com.github.dakusui.jcunit.core.ConfigUtils;
import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.lang.reflect.Field;
import java.util.LinkedList;
import java.util.List;

public class FactorLoader {
    private final Field field;
    private ValidationResult validationResult = null;

    public FactorLoader(Field f) {
        this.field = f;
    }

    public ValidationResult validate() {
        return this.validate(this.field);
    }

    ValidationResult validate(Field f) {
        Utils.checknotnull(f);
        FactorField ann = f.getAnnotation(FactorField.class);
        Utils.checknotnull(ann);
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
            levelsProvider.init(ConfigUtils.processParams(levelsProvider.parameterTypes(), ann.providerParams()));
            ret = new ValidationResult(true, levelsProvider, null);
        } else {
            ret = new ValidationResult(false, null,
                    Utils.join("; ", errors.toArray()));
        }
        return ret;
    }

    public Factor getFactor() {
        if (this.validationResult == null) {
            this.validationResult = this.validate(this.field);
        }
        if (!this.validationResult.isValid()) {
            throw new FactorFieldValidationException(this.validationResult);
        }
        LevelsProvider<?> levelsProvider = this.validationResult
                .getLevelsProvider();

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
                Utils.checknotnull(levelsProvider);
            } else {
                Utils.checknotnull(errorMessage);
            }
            this.valid = valid;
            this.levelsProvider = levelsProvider;
            this.errMessage = errorMessage;
        }

        public boolean isValid() {
            return this.valid;
        }

        /**
         * This method returns null, if this object represents a valid result.
         */
        public String getErrorMessage() {
            return this.errMessage;
        }

        public LevelsProvider<?> getLevelsProvider() {
            return levelsProvider;
        }

        public void check() {
            if (this.valid) {
                throw new FactorFieldValidationException(this);
            }
        }
    }

    public static class FactorFieldValidationException
            extends JCUnitException {
        private final ValidationResult validationResult;

        /**
         * Creates an object of this class.
         */
        public FactorFieldValidationException(
                ValidationResult result) {
            super(Utils.checknotnull(result).getErrorMessage(), null);
            this.validationResult = result;
        }

        public ValidationResult getValidationResult() {
            return this.validationResult;
        }
    }
}
