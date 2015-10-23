package com.github.dakusui.jcunit.standardrunner.annotations;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.plugins.Plugin;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

@Retention(RetentionPolicy.RUNTIME)
public @interface Arg {
  Arg[] EMPTY_ARRAY = new ArrayBuilder().build();

  class Builder {
    private String[] values;

    public Builder() {
    }

    public Builder add(String... values) {
      this.values = values;
      return this;
    }

    public Arg build() {
      return new Arg() {
        @Override
        public Class<? extends Annotation> annotationType() {
          return Arg.class;
        }

        @Override
        public String[] value() {
          return Builder.this.values;
        }
      };
    }
  }

  class ArrayBuilder {
    private final List<Arg> args = new LinkedList<Arg>();

    public ArrayBuilder() {
    }

    public ArrayBuilder add(String... values) {
      args.add(new Builder().add(values).build());
      return this;
    }

    public Arg[] build() {
      return this.args.toArray(new Arg[this.args.size()]);
    }
  }

  String[] value();

  class Translator extends Plugin.Param.Translator<Arg> {
    protected Translator(List<Plugin.Param.Converter<Arg>> converters) {
      super(converters);
    }

    @Override
    protected <T> Plugin.Param.Converter<Arg> chooseConverter(Class<T> clazz, List<Plugin.Param.Converter<Arg>> from) {
      return from.get(0);
    }
  }

  abstract class Type implements Cloneable {
    private static Object NO_DEFAULT_VALUE;
    protected      Object defaultValue;
    private boolean varArgs = false;

    protected Type() {
      synchronized (Type.class) {
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

    public Type withDefaultValue(Object defaultValue) {
      Type ret;
      ret = this.cloneObject();
      ret.defaultValue = defaultValue;
      return ret;
    }

    public Type withVarArgsEnabled() {
      Type ret;
      ret = this.cloneObject();
      ret.varArgs = true;
      return ret;
    }

    abstract public Object parse(String[] values);

    protected Type cloneObject() {
      try {
        return (Type) this.clone();
      } catch (CloneNotSupportedException e) {
        Checks.checkcond(false);
      }
      // This line will never be executed.
      throw new RuntimeException("Something went wrong.");
    }

    /**
     * TODO: update this javadoc.
     * Initializes this object.
     *
     * Users of the implementations of this interface must call this method right
     * after this class is instantiated.
     * <p/>
     * Until this method is called, behaviors of any other methods will not be predictable.
     *
     * The parameters ({@code processedParameters}) are values that are already
     * validated and translated into ones the users (and the plug-in) originally intended by
     * using {@code @Params} annotations.
     *
     * That is, if an annotation below is given,
     *
     * <pre>
     *  args = {
     *      {@literal @}Param("2")
     *  }),
     * </pre>
     *
     * And the {@code parameterTypes} returns
     *
     * <pre>
     *   new ParamType[]{ ParamType.Int }
     * </pre>
     *
     * then, the user's intention is to pass an int value 2 to this plug in.
     *
     * So the {@code processedParameters} will be an array whose first and only element
     * is an int, 2.
     *
     * @param args An array of parameter values.
     */
    public static Object[] processParams(Type[] types, Arg[] args) {
      Checks.checknotnull(args);
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
        Checks.checktest(minLength <= args.length && args.length <= types.length,
            "Too little or too many number of parameters (at least %d and %d at maximum required, but %d given).: %s",
            minLength,
            types.length,
            args.length,
            Arrays.toString(args)
        );
      } else {
        Checks.checktest(minLength <= args.length,
            "Too little number of parameters (at least %d required, but %d given).: %s",
            minLength,
            args.length,
            Arrays.toString(args)
        );
      }
      Object[] ret = new Object[Math.max(types.length, args.length)];
      int i = 0;
      boolean varArgsDefined = false;
      boolean varArgsParameterPresent = false;
      for (Type t : types) {
        if (i >= args.length) {
          if (t.hasDefaultValue()) {
            ret[i] = t.defaultValue();
          } else if (t.isVarArgs()) {
            Checks.checkplugin(i == types.length - 1,
                "Var args parameter can only be placed at the last of parameters.");
            varArgsDefined = true;
            break;
          } else {
            Checks.checkplugin(false, "Failed to parse %s (%s) in %d",
                Arrays.toString(args), Arrays.toString(types), i);
          }
        } else {
          try {
            if (!t.isVarArgs()) {
              ret[i] = t.parse(args[i].value());
            } else {
              Checks.checkplugin(i == types.length - 1,
                  "Var args parameter can only be placed at the last of parameters.");
              varArgsDefined = true;
              while (i < args.length) {
                ret[i] = t.parse(args[i].value());
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
                    Arrays.toString(args[i].value()),
                    types[i],
                    i,
                    Arrays.toString(args)
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

    public static abstract class NonArrayType extends Type {
      protected NonArrayType() {
        super();
      }

      @Override
      public Object parse(String[] parameters) {
        checkParameters(parameters);
        try {
          return this.parse(parameters[0]);
        } catch (IllegalArgumentException e) {
          Checks.rethrowtesterror(e, "Invalid parameter(s) are given.: %s", Utils.join(",", new Object[] { parameters }));
        }
        // This path should never be executed.
        throw new RuntimeException("Something went wrong");
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

    public static class ArrayType extends Type {
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
        Checks.checkparam("false".equals(str) || "true".equals(str),
            "Only 'true' and 'false' are acceptable here.");
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

    public static final NonArrayType Short = new NonArrayType() {
      @Override
      protected Object parse(String str) {
        return java.lang.Short.parseShort(str);
      }

      @Override
      public String toString() {
        return "short";
      }
    };
    public static final NonArrayType Int   = new NonArrayType() {
      @Override
      protected Object parse(String str) {
        return Integer.parseInt(str);
      }

      @Override
      public String toString() {
        return "int";
      }
    };
    public static final NonArrayType Long  = new NonArrayType() {
      @Override
      protected Object parse(String str) {
        return java.lang.Long.parseLong(str);
      }

      @Override
      public String toString() {
        return "long";
      }
    };
    public static final NonArrayType Float = new NonArrayType() {
      @Override
      protected Object parse(String str) {
        return java.lang.Float.parseFloat(str);
      }

      @Override
      public String toString() {
        return "float";
      }
    };

    public static final NonArrayType Double = new NonArrayType() {
      @Override
      protected Object parse(String str) {
        return java.lang.Double.parseDouble(str);
      }

      @Override
      public String toString() {
        return "double";
      }
    };

    public static final NonArrayType String = new NonArrayType() {
      @Override
      protected Object parse(String str) {
        return str;
      }

      @Override
      public String toString() {
        return "String";
      }
    };

    private static class DefaultValue {
    }

    public static final Type BooleanArray = new ArrayType(Type.Boolean);
    public static final Type ByteArray    = new ArrayType(Type.Byte);
    public static final Type CharArray    = new ArrayType(Type.Char);
    public static final Type ShortArray   = new ArrayType(Type.Short);
    public static final Type IntArray     = new ArrayType(Type.Int);
    public static final Type LongArray    = new ArrayType(Type.Long);
    public static final Type FloatArray   = new ArrayType(Type.Float);
    public static final Type DoubleArray  = new ArrayType(Type.Double);
    public static final Type StringArray  = new ArrayType(Type.String);
  }
}
