package com.github.dakusui.jcunit.runners.standard.annotations;


import com.github.dakusui.jcunit.core.StringUtils;
import com.github.dakusui.jcunit.core.Utils;
import org.junit.Test;
import org.junit.runners.model.FrameworkField;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.METHOD)
@ValidateWith(Uses.Validator.class)
public @interface Uses {
  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      List<Exception> errors = new LinkedList<Exception>();
      if (method.getAnnotation(Test.class) == null && method.getAnnotation(Condition.class) == null) {
        errors.add(new Exception(StringUtils.format(
            "This annotation should be used with @%s or @%s annotation.", Test.class, Condition.class
        )));
      }

      List<String> factorFieldNames = Utils.transform(
          new TestClass(method.getDeclaringClass()).getAnnotatedFields(FactorField.class),
          new Utils.Form<FrameworkField, String>() {
            @Override
            public String apply(FrameworkField in) {
              return in.getName();
            }
          });
      for (String eachFieldName : method.getAnnotation(Uses.class).value()) {
        if ("*".equals(eachFieldName))
          continue;
        if (!factorFieldNames.contains(eachFieldName)) {
          errors.add(new Exception(String.format(
              "'%s' factor field referenced by @Uses annotation attached to '%s' method was not found in '%s' class. (Or not annotated with @FactorField)",
              eachFieldName,
              method.getName(),
              method.getDeclaringClass().getCanonicalName()
              )));
        }
      }
      return errors;
    }
  }

  String[] value() default { "*" };
}
