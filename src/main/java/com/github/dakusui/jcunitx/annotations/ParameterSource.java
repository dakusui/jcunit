package com.github.dakusui.jcunitx.annotations;

import com.github.dakusui.jcunitx.engine.junit5.JCUnitExtension;
import org.apiguardian.api.API;
import org.junit.jupiter.api.TestTemplate;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runners.model.FrameworkMethod;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.Documented;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;

import static java.lang.annotation.ElementType.ANNOTATION_TYPE;
import static java.lang.annotation.ElementType.METHOD;
import static org.apiguardian.api.API.Status.STABLE;

@Retention(RetentionPolicy.RUNTIME)
@ValidateWith(ParameterSource.Validator.class)
@Target({ METHOD, ANNOTATION_TYPE })
@Documented
@API(status = STABLE, since = "5.0")
@ExtendWith(JCUnitExtension.class)
@TestTemplate // To suppress "unused warnings" for methods with this annotation.
public @interface ParameterSource {
  class Validator extends AnnotationValidator {
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      return new LinkedList<Exception>() {{
        if (!method.isPublic())
          add(new Exception(
              String.format("'%s' must be public but not.", method.getName())
          ));
        if (method.getMethod().getParameterCount() > 0) {
          add(new Exception(
              String.format("'%s' must not have any parameter but has one or more.", method.getName())
          ));
        }
        if (method.isStatic()) {
          add(new Exception(
              String.format("'%s' must not be static but is.", method.getName())
          ));
        }
      }};
    }
  }
}
