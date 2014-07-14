package com.github.dakusui.jcunit.core;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.util.Arrays;

@Retention(RetentionPolicy.RUNTIME)
public @interface Param {
  String[] value();

  Type type() default Type.String;

  boolean array() default false;

  public static enum Type {
    Boolean {
      @Override public Object parse(String str) {
        return java.lang.Boolean.parseBoolean(str);
      }

      @Override public Object createArray(int length) {
        return new boolean[length];
      }

    },
    Byte {
      @Override public Object parse(String str) {
        return java.lang.Byte.parseByte(str);
      }

      @Override public Object createArray(int length) {
        return new byte[length];
      }
    },
    Char {
      @Override public Object parse(String str) {
        Utils.checkcond(str.length() == 1);
        return (Character) str.charAt(0);
      }

      @Override public Object createArray(int length) {
        return new char[length];
      }
    },
    Short {
      @Override public Object parse(String str) {
        return java.lang.Short.parseShort(str);
      }

      @Override public Object createArray(int length) {
        return new short[length];
      }
    },
    Int {
      @Override public Object parse(String str) {
        return java.lang.Integer.parseInt(str);
      }

      @Override public Object createArray(int length) {
        return new int[length];
      }
    },
    Long {
      @Override public Object parse(String str) {
        return java.lang.Short.parseShort(str);
      }

      @Override public Object createArray(int length) {
        return new long[length];
      }
    },
    Float {
      @Override public Object parse(String str) {
        return java.lang.Float.parseFloat(str);
      }

      @Override public Object createArray(int length) {
        return new float[length];
      }
    },
    Double {
      @Override public Object parse(String str) {
        return java.lang.Double.parseDouble(str);
      }

      @Override public Object createArray(int length) {
        return new double[length];
      }
    },
    String {
      @Override public Object parse(String str) {
        return str;
      }
      @Override public Object createArray(int length) {
        return new String[length];
      }
    }
    ;

    public abstract Object parse(String str);

    public abstract Object createArray(int length);

    public Object getValue(Param param) {
      Utils.checknotnull(param);
      Object ret;
      int len = param.value().length;
      if (param.array()) {
        ret = createArray(len);
        for (int i = 0; i < len; i++) {
          Array.set(ret, i, param.value()[i]);
        }
      } else {
        Utils.checkcond(len == 1, java.lang.String.format(
            "Each parameter must have one (and only one) value if it is marked 'array = true', but %d value(s) found.: %s",
            len,
            Arrays.toString(param.value())));
        ret = parse(param.value()[0]);
      }
      return ret;
    }
  }
}
