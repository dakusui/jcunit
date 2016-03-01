package com.github.dakusui.jcunit.runners.standard;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.fsm.FSMUtils;
import org.junit.runners.model.FrameworkMethod;

import java.util.LinkedList;
import java.util.List;

/**
 * This class represents a composite framework method (And and Or).
 */
public class CompositeFrameworkMethod extends FrameworkMethodUtils.JCUnitFrameworkMethod {
  public static class Builder {
    private List<FrameworkMethod> methods = new LinkedList<FrameworkMethod>();
    Mode mode = null;

    public Builder(Mode mode) {
      this.mode = Checks.checknotnull(mode);
    }

    public Builder addMethod(boolean negate, FrameworkMethod method) {
      FrameworkMethod m;
      if (negate) {
        m = new FrameworkMethodUtils.NegatedFrameworkMethod(method);
      } else {
        m = method;
      }
      methods.add(m);
      return this;
    }

    public CompositeFrameworkMethod build() {
      return new CompositeFrameworkMethod(this.mode, this.methods);
    }
  }

  public enum Mode {
    And {
      public String toString() {
        return "&&";
      }
    },
    Or {
      public String toString() {
        return "||";
      }
    }
  }

  private final Mode                  mode;
  private final List<FrameworkMethod> methods;

  /**
   * Returns a new {@code FrameworkMethod} for {@code method}
   */
  public CompositeFrameworkMethod(Mode mode, List<FrameworkMethod> methods) {
    super(DUMMY_METHOD);
    Checks.checknotnull(methods);
    Checks.checknotnull(mode, "Mode isn't set yet.");
    this.methods = methods;
    this.mode = mode;
  }

  @Override
  public Object invokeExplosively(final Object target, final Object... params) throws Throwable {
    if (mode == Mode.And) {
      boolean ret = true;
      for (FrameworkMethod each : this.methods) {
        ret &= (Boolean) each.invokeExplosively(target, params);
      }
      return ret;
    } else if (mode == Mode.Or) {
      boolean ret = false;
      for (FrameworkMethod each : this.methods) {
        FSMUtils.resetStories(target);
        ret |= (Boolean) each.invokeExplosively(target, params);
      }
      return ret;
    }
    assert false;
    return null;
  }

  @Override
  public String getName() {
    StringBuilder b = new StringBuilder();
    if (this.methods.size() > 1) {
      b.append("(");
    }
    boolean firstTime = true;
    for (FrameworkMethod each : this.methods) {
      if (!firstTime) {
        b.append(this.mode);
      }
      b.append(each.getName());
      firstTime = false;
    }
    if (this.methods.size() > 1) {
      b.append(")");
    }
    return b.toString();
  }

  @SuppressWarnings("unused")
  public static boolean dummyMethod() {
    return true;
  }
}
