package com.github.dakusui.jcunit.core;

import java.util.Arrays;

public abstract class ParamType  implements Cloneable {
  public static abstract class NonArrayType extends ParamType {
    @Override
    public Object parse(String[] parameters) {
      checkParameters(parameters);
      return this.parse(parameters[0]);
    }

    protected void checkParameters(String[] parameters) {
      Utils.checknotnull(parameters);
      ConfigUtils.checkParam(parameters.length == 1,
          "This parameter needs to be a non-array '%s', but '%s' (an array whose length is '%d') was given",
          this.getClass().getSimpleName(), Arrays.toString(parameters),
          parameters.length
      );
    }
    protected abstract Object parse(String str);
  }

  public static class ArrayType extends ParamType {
    private final NonArrayType enclosedType;

    public ArrayType(NonArrayType enclosedType) {
      Utils.checknotnull(enclosedType);
      this.enclosedType = enclosedType;
    }

    final public Object parse(String[] parameters) {
      Utils.checknotnull(parameters);
      Object[] ret = new Object[parameters.length];
      int i = 0;
      for (String s : parameters) {
        ret[i++] = enclosedType.parse(s);
      }
      return ret;
    }
  }

  public static final NonArrayType Boolean = new NonArrayType() {
    @Override protected Object parse(String str) {
        Utils.checkparam("false".equals(str) || "true".equals(str), "Only 'true' and 'false' are acceptable here.");
        return java.lang.Boolean.parseBoolean(str);
    }

    @Override public String toString() {
      return "boolean";
    }
  };
  public static final NonArrayType Byte    = new NonArrayType() {
    @Override protected Object parse(String str) {
      return java.lang.Byte.parseByte(str);
    }

    @Override public String toString() {
      return "byte";
    }
  };
  public static final NonArrayType Char    = new NonArrayType() {
    @Override protected Object parse(String str) {
      Utils.checkparam(str.length() == 1);
      return str.charAt(0);
    }

    @Override public String toString() {
      return "char";
    }
  };
  public static final NonArrayType Short   = new NonArrayType() {
    @Override protected Object parse(String str) {
      return java.lang.Short.parseShort(str);
    }

    @Override public String toString() {
      return "short";
    }
  };
  public static final NonArrayType Int     = new NonArrayType() {
    @Override protected Object parse(String str) {
      return Integer.parseInt(str);
    }

    @Override public String toString() {
      return "int";
    }
  };
  public static final NonArrayType Long    = new NonArrayType() {
    @Override protected Object parse(String str) {
      return java.lang.Long.parseLong(str);
    }

    @Override public String toString() {
      return "long";
    }
  };
  public static final NonArrayType Float   = new NonArrayType() {
    @Override protected Object parse(String str) {
      return java.lang.Float.parseFloat(str);
    }

    @Override public String toString() {
      return "float";
    }
  };
  public static final NonArrayType Double  = new NonArrayType() {
    @Override protected Object parse(String str) {
      return java.lang.Double.parseDouble(str);
    }

    @Override public String toString() {
      return "double";
    }
  };
  public static final NonArrayType String  = new NonArrayType() {
    @Override protected Object parse(String str) {
      return str;
    }

    @Override public String toString() {
      return "String";
    }
  };

  public static final ParamType BooleanArray = new ArrayType(Boolean);
  public static final ParamType ByteArray    = new ArrayType(Byte);
  public static final ParamType CharArray    = new ArrayType(Char);
  public static final ParamType ShortArray   = new ArrayType(Short);
  public static final ParamType IntArray     = new ArrayType(Int);
  public static final ParamType LongArray    = new ArrayType(Long);
  public static final ParamType FloatArray   = new ArrayType(Float);
  public static final ParamType DoubleArray  = new ArrayType(Double);
  public static final ParamType StringArray  = new ArrayType(String);

  protected            Object defaultValue     = NO_DEFAULT_VALUE;
  private static final Object NO_DEFAULT_VALUE = new Object();

  public boolean hasDefaultValue() {
    return !(this.defaultValue == NO_DEFAULT_VALUE);
  }

  public Object defaultValue() {
    Utils.checkcond(this.hasDefaultValue());
    return this.defaultValue;
  }

  public boolean isVarArgs() {
    return false;
  }

  public ParamType withDefaultValue(Object defaultValue) {
    ParamType ret;
    try {
      ret = (ParamType) this.clone();
      ret.defaultValue = defaultValue;
      return ret;
    } catch (CloneNotSupportedException e) {
      Utils.rethrow(e);
    }
    throw new RuntimeException(); // This line will never be executed.
  }

  abstract public Object parse(String[] values);
}
