package com.github.dakusui.jcunit8.runners.junit4.annotations;

import com.github.dakusui.jcunit8.runners.core.NodeUtils;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.TestClass;
import org.junit.validator.AnnotationValidator;
import org.junit.validator.ValidateWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.util.LinkedList;
import java.util.List;
import java.util.Objects;

import static java.lang.String.format;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

@Retention(RUNTIME)
@Target(value = ElementType.METHOD)
@ValidateWith(Given.Validator.class)
public @interface Given {
  String ALL_CONSTRAINTS = "*";

  /**
   * Returns an array of method names within the same class this annotation is
   * attached to.
   *
   * @return Names of all methods to that this annotation is attached.
   */
  String[] value() default { ALL_CONSTRAINTS };

  class Validator extends AnnotationValidator {
    @Override
    public List<Exception> validateAnnotatedMethod(FrameworkMethod method) {
      return validate(method.getAnnotation(Given.class), new TestClass(method.getDeclaringClass()));
    }

    static List<Exception> validate(Given instance, TestClass testClass) {
      return new LinkedList<Exception>() {{
        NodeUtils.allLeaves(instance.value())
            .forEach((String s) -> {
              // "matches(helloWorld)@[a]"
              if (!s.matches("[A-Za-z_$][A-Za-z0-9_$]*"))
                add(new Exception(format("'%s' is not a valid condition name", s)));
              else {
                if (testClass.getAnnotatedMethods(Condition.class).stream()
                    .noneMatch(frameworkMethod -> Objects.equals(frameworkMethod.getName(), s)))
                  add(new Exception(format("'%s' was not found in '%s'", s, testClass.getJavaClass().getSimpleName())));
              }
            });
      }};
    }
  }

}
