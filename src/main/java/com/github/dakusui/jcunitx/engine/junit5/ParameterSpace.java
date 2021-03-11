package com.github.dakusui.jcunitx.engine.junit5;

import com.github.dakusui.jcunitx.model.condition.Constraint;
import com.github.dakusui.jcunitx.model.factor.FactorSpace;
import com.github.dakusui.jcunitx.model.parameter.Parameter;

import java.util.List;

public interface ParameterSpace {
  List<Parameter<?>> parameters();

  List<Constraint> constraints();

  List<String> childNames();

  ParameterSpace child(String childName);

  FactorSpace toFactorSpace();

  interface Factory {
    ParameterSpace create(TestMethod testMethod);

    class AnnotationBased implements Factory {
      @Override
      public ParameterSpace create(TestMethod testMethod) {
        return null;
      }
    }
  }
}
