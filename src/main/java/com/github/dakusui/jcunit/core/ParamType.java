package com.github.dakusui.jcunit.core;

import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.util.Arrays;

public abstract class ParamType implements Cloneable {
  public static Object[] processParams(ParamType[] types, Param[] params) {
    Checks.checknotnull(params);
    Checks.checknotnull(types);
    int minLength = types.length;
    boolean varArgsSpecified = false;
    if (minLength > 0 && types[minLength - 1].isVarArgs()) {
      varArgsSpecified = true;
      minLength--;
    }
    while (minLength > 0) {
      if (types[minLength - 1].hasDefaultValue()) {
        minLength--;
      } else {
        break;
      }
    }
    for (int i = 0; i < minLength; i++) {
      Checks.checkplugin(!types[i].hasDefaultValue(),
          "Only the last parameters of a plugin can have default values.: %s",
          Arrays.toString(types));
    }
    if (!varArgsSpecified) {
      Checks.checktest(minLength <= params.length && params.length <= types.length,
          "Too little or too many number of parameters (at least %d and %d at maximum required, but %d given).: %s",
          minLength,
          types.length,
          params.length,
          Arrays.toString(params)
      );
    } else {
      Checks.checktest(minLength <= params.length,
          "Too little number of parameters (at least %d required, but %d given).: %s",
          minLength,
          params.length,
          Arrays.toString(params)
      );
    }
    Object[] ret = new Object[Math.max(types.length, params.length)];
    int i = 0;
    boolean varArgsDefined = false;
    boolean varArgsParameterPresent = false;
    for (ParamType t : types) {
      if (i >= params.length) {
        if (t.hasDefaultValue()) {
          ret[i] = t.defaultValue();
        } else if (t.isVarArgs()) {
          Checks.checkplugin(i == types.length - 1,
              "Var args parameter can only be placed at the last of parameters.");
          varArgsDefined = true;
          break;
        } else {
          Checks.checkplugin(false, "Failed to parse %s (%s) in %d",
              Arrays.toString(params), Arrays.toString(types), i);
        }
      } else {
        try {
          if (!t.isVarArgs()) {
            ret[i] = t.parse(params[i].value());
          } else {
            Checks.checkplugin(i == types.length - 1,
                "Var args parameter can only be placed at the last of parameters.");
            varArgsDefined = true;
            while (i < params.length) {
              ret[i] = t.parse(params[i].value());
              varArgsParameterPresent = true;
              i++;
            }
            break;
          }
        } catch (JCUnitException e) {
          throw e;
        } catch (RuntimeException e) {
          throw e;
        } catch (Exception e) {
          Checks.rethrow(e,
              java.lang.String.format(
                  "The given value '%s' can't be converted to '%s' value.: %dth value in %s",
                  Arrays.toString(params[i].value()),
                  types[i],
                  i,
                  Arrays.toString(params)
              ));
        }
      }
      i++;
    }
    if (varArgsDefined && !varArgsParameterPresent) {
      ////
      // In case the last param is var args and no value is given to
      // it, nothing should be appended to the returned parameter values.
      ret = Arrays.copyOfRange(ret, 0, ret.length - 1);
    }
    return ret;
  }

  public static abstract class NonArrayType extends ParamType {
    protected NonArrayType() {
      super();
    }

    @Override
    public Object parse(String[] parameters) {
      checkParameters(parameters);
      return this.parse(parameters[0]);
    }

    protected void checkParameters(String[] parameters) {
      Checks.checknotnull(parameters);
      Checks.checktest(parameters.length == 1,
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
      super();
      Checks.checknotnull(enclosedType);
      this.enclosedType = enclosedType;
    }

    final public Object parse(String[] parameters) {
      Checks.checknotnull(parameters);
      Object[] ret = new Object[parameters.length];
      int i = 0;
      for (String s : parameters) {
        ret[i++] = enclosedType.parse(s);
      }
      return ret;
    }
  }

  public static final NonArrayType Boolean = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      Checks.checkparam("false".equals(str) || "true".equals(str), "Only 'true' and 'false' are acceptable here.");
      return java.lang.Boolean.parseBoolean(str);
    }

    @Override
    public String toString() {
      return "boolean";
    }
  };
  public static final NonArrayType Byte    = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      return java.lang.Byte.parseByte(str);
    }

    @Override
    public String toString() {
      return "byte";
    }
  };
  public static final NonArrayType Char    = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      Checks.checkparam(str.length() == 1);
      return str.charAt(0);
    }

    @Override
    public String toString() {
      return "char";
    }
  };
  public static final NonArrayType Short   = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      return java.lang.Short.parseShort(str);
    }

    @Override
    public String toString() {
      return "short";
    }
  };
  public static final NonArrayType Int     = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      return Integer.parseInt(str);
    }

    @Override
    public String toString() {
      return "int";
    }
  };
  public static final NonArrayType Long    = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      return java.lang.Long.parseLong(str);
    }

    @Override
    public String toString() {
      return "long";
    }
  };
  public static final NonArrayType Float   = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      return java.lang.Float.parseFloat(str);
    }

    @Override
    public String toString() {
      return "float";
    }
  };
  public static final NonArrayType Double  = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      return java.lang.Double.parseDouble(str);
    }

    @Override
    public String toString() {
      return "double";
    }
  };
  public static final NonArrayType String  = new NonArrayType() {
    @Override
    protected Object parse(String str) {
      return str;
    }

    @Override
    public String toString() {
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

  private static Object NO_DEFAULT_VALUE;
  protected Object defaultValue;
  private boolean varArgs = false;

  protected ParamType() {
    synchronized (ParamType.class) {
      NO_DEFAULT_VALUE = DefaultValue.class;
    }
    this.defaultValue = NO_DEFAULT_VALUE;
  }

  public boolean hasDefaultValue() {
    return !(this.defaultValue == NO_DEFAULT_VALUE);
  }

  public Object defaultValue() {
    Checks.checkcond(this.hasDefaultValue());
    return this.defaultValue;
  }

  public boolean isVarArgs() {
    return this.varArgs;
  }

  public ParamType withDefaultValue(Object defaultValue) {
    ParamType ret;
    ret = this.clone();
    ret.defaultValue = defaultValue;
    return ret;
  }

  public ParamType withVarArgsEnabled() {
    ParamType ret;
    ret = this.clone();
    ret.varArgs = true;
    return ret;
  }

  abstract public Object parse(String[] values);

  protected ParamType clone() {
    try {
      return (ParamType) super.clone();
    } catch (CloneNotSupportedException e) {
      Checks.checkcond(false);
    }
    // This line will never be executed.
    throw new RuntimeException("Something went wrong.");
  }

  private static class DefaultValue {}
}
