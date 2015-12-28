package com.github.dakusui.jcunit.examples.models.calc;

public class Calc {
  public enum Op {
    PLUS {
      @Override
      int calc(int a, int b) {
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
    MINUS {
      @Override
      int calc(int a, int b) {
        return a - b;
      }

      @Override
      public String str() {
        return "-";
      }
    },
    MULTIPLY {
      @Override
      int calc(int a, int b) {
        return a * b;
      }

      @Override
      public String str() {
        return "*";
      }
    },
    DIVIDE {
      @Override
      int calc(int a, int b) {
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
