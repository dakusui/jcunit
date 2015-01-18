package com.github.dakusui.jcunit.examples.fsm;

import com.github.dakusui.jcunit.core.FactorField;
import com.github.dakusui.jcunit.core.Param;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.factor.LevelsProvider;

import java.lang.reflect.Field;
import java.util.List;

public class FSMLevelsProvider implements LevelsProvider {
  @Override
  public int size() {
    return 0;
  }

  @Override
  public Object get(int n) {
    return null;
  }

  @Override
  public void setTargetField(Field targetField) {

  }

  @Override
  public void setAnnotation(FactorField ann) {

  }

  @Override
  public List<String> getErrorsOnInitialization() {
    return null;
  }

  @Override
  public void init(Param[] params) {

  }

  @Override
  public ParamType[] parameterTypes() {
    return new ParamType[0];
  }

  @Override
  public Param[] getParams() {
    return new Param[0];
  }
}
