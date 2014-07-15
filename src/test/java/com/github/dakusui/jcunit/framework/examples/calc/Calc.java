package com.github.dakusui.jcunit.framework.examples.calc;

public class Calc {
  public static enum Op {
    plus {
      @Override int calc(int a, int b) {
        int ret = a + b;
        if (Math.signum(a) == Math.signum(b)
            && Math.signum(ret) != Math.signum(a)) {
          throw new RuntimeException();
        }
        return a + b;
      }

      @Override
      public String str() {
        return "+";
      }
    },
    minus {
      @Override int calc(int a, int b) {
        return a - b;
      }

      @Override
      public String str() {
        return "-";
      }
    },
    multiply {
      @Override int calc(int a, int b) {
        return a * b;
      }

      @Override
      public String str() {
        return "*";
      }
    },
    divide {
      @Override int calc(int a, int b) {
        return a / b;
      }

      @Override
      public String str() {
        return "/";
      }
    };

    abstract int calc(int a, int b);

    abstract public String str();
  }

  public int calc(Op op, int a, int b) {
    return op.calc(a, b);
  }
}
