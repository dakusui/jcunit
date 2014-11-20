package com.github.dakusui.jcunit.fsm;

public interface FactorNameResolver {
  public String stateName(int i);

  public String actionName(int i);

  public int numParams(String actionName);

  public String paramName(String actionName, int i);
}
