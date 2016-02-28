package com.github.dakusui.jcunit.runners.standard.annotations;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.FSMLevelsProvider;
import com.github.dakusui.jcunit.fsm.Story;
import com.github.dakusui.jcunit.plugins.levelsproviders.LevelsProvider;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.LinkedList;
import java.util.List;

import static com.github.dakusui.jcunit.core.Checks.checknotnull;

@Target({ ElementType.TYPE, ElementType.FIELD })
@Retention(RetentionPolicy.RUNTIME)
@ValidateWith(GenerateCoveringArrayWith.Validator.class)
public @interface GenerateCoveringArrayWith {
  Generator engine() default @Generator();

  Checker checker() default @Checker();

  Reporter[] reporters() default {};

  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedClass(TestClass testClass) {
      ////
      // TODO: Issue-#45
      return super.validateAnnotatedClass(testClass);
    }

    @Override
    public List<Exception> validateAnnotatedField(FrameworkField f) {
      List<Exception> errors = new LinkedList<Exception>();
      ////
      // The following logic is for validating so called 'FSMfactor fields' , whose
      // type is Story<FSMSpec<SUT>, SUT>.
      checknotnull(f);
      FactorField ann = f.getAnnotation(FactorField.class);
      checknotnull(ann);
      Checks.checkcond(ann.levelsProvider() != null);
      Class<? extends LevelsProvider> levelsProvider = ann.levelsProvider();
      checknotnull(levelsProvider);
      Checks.checkcond(
          FSMLevelsProvider.class.isAssignableFrom(levelsProvider),
          "'%s' must be a sub-class of '%s', but isn't",
          levelsProvider.getCanonicalName(),
          FSMLevelsProvider.class.getCanonicalName()
      );
      ////
      // Another design choice is to allow sub types of Story for FSM factor fields.
      // But author considered it hurts readability of tests and thus allowed
      // to use Story<SUT, FSMSpec<SUT>> directly.
      if (!(Story.class.equals(f.getType()))) {
        errors.add(new Exception(String.format(
            "For FSM factor field (field annotated with '%s' whose levelsProvider is '%s') must be exactly '%s', but was '%s'",
            FactorField.class.getSimpleName(),
            FSMLevelsProvider.class.getSimpleName(),
            Story.class.getCanonicalName(),
            f.getType()
        )));
      }
      Type genericType = f.getField().getGenericType();
      if (!(genericType instanceof ParameterizedType)) {
        errors.add(new Exception(String.format(
            "FSM factor field must have a parameterized type as its generic type. But '%s'(%s)'s generic type was '%s'",
            f.getName(),
            f.getDeclaringClass().getCanonicalName(),
            genericType != null
                ? genericType.getClass().getCanonicalName()
                : null
        )));
      }
      return errors;
    }
  }
}

