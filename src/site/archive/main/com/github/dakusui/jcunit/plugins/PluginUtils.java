package com.github.dakusui.jcunit.plugins;

import com.github.dakusui.jcunit.core.utils.Checks;
import com.github.dakusui.jcunit.core.CompatUtils;
import com.github.dakusui.jcunit.plugins.Plugin.Param.Converter;
import com.github.dakusui.jcunit.runners.core.RunnerContext;

import java.lang.reflect.Array;
import java.util.*;

public enum PluginUtils {
  ;

  public static boolean str2boolean(String s) {
    Checks.checknotnull(s);
    return Boolean.parseBoolean(s);
  }

  public static byte str2byte(String s) {
    Checks.checknotnull(s);
    return Byte.parseByte(s);
  }

  public static char str2char(String s) {
    Checks.checknotnull(s);
    Checks.checkcond(s.length() == 1);
    return s.charAt(0);
  }

  public static short str2short(String s) {
    Checks.checknotnull(s);
    return Short.parseShort(s);
  }

  public static int str2int(String s) {
    Checks.checknotnull(s);
    return Integer.parseInt(s);
  }

  public static long str2long(String s) {
    Checks.checknotnull(s);
    return Long.parseLong(s);
  }

  public static float str2float(String s) {
    Checks.checknotnull(s);
    return Float.parseFloat(s);
  }

  public static double str2double(String s) {
    Checks.checknotnull(s);
    return Double.parseDouble(s);
  }

  public static Class<?> str2Class(String s) {
    Checks.checknotnull(s);
    try {
      return Class.forName(s);
    } catch (ClassNotFoundException e) {
      throw Checks.wrap(e);
    }
  }

  public static class StringArrayResolver extends Plugin.Param.Resolver<String[]> {
    public static StringArrayResolver INSTANCE = new StringArrayResolver();

    public StringArrayResolver() {
      super(createBuiltInConverters());
    }

    private static List<Converter<String[]>> createBuiltInConverters() {
      List<Converter<String[]>> ret = new LinkedList<Converter<String[]>>();
      for (final Converter<String> each : StringResolver.INSTANCE.allConverters()) {
        ////
        // If the model is a supported non-array type, the first element in {@code in}
        // will be picked up and assigned.
        ret.add(new Converter<String[]>() {
          @Override
          public Object convert(Class requested, String[] in) {
            Checks.checktest(in.length > 0, "Insufficient number of config values for a plug-in");
            Checks.checktest(in.length == 1, "Too many config values values for a plug-in: %s", Arrays.toString(in));
            return each.convert(requested, in[0]);
          }

          @Override
          public boolean supports(Class<?> target) {
            return each.supports(target);
          }
        });
        ////
        // If the model type is an array of supported type of StringResolver,
        // it's supported.
        ret.add(new Converter<String[]>() {
          @Override
          public Object convert(Class requested, String[] in) {
            ////
            // Create an array
            Object ret = Array.newInstance(requested.getComponentType(), in.length);
            int i = 0;
            for (String s : in) {
              Array.set(ret, i, each.convert(requested.getComponentType(), s));
              i++;
            }

            return ret;
          }

          @Override
          public boolean supports(Class<?> target) {
            return target.isArray() && each.supports(target.getComponentType());
          }
        });
      }
      ret.add(new Converter<String[]>() {
        @Override
        public Object convert(Class requested, String[] in) {
          Checks.checktest(in.length > 0, "Missing value");
          String className = in[0];
          try {
            //noinspection unchecked
            Class<Plugin> tupleGeneratorClass = (Class<Plugin>) Class.forName(className);
            Plugin.Factory<Plugin, String> factory = new Plugin.Factory<Plugin, String>(
                tupleGeneratorClass,
                StringResolver.INSTANCE,
                RunnerContext.DUMMY
                );
            return factory.create(Arrays.asList(in).subList(1, in.length));
          } catch (ClassNotFoundException e) {
            throw Checks.wrap(e, "class '%s' is not found on the classpath.", className);
          }
        }

        @Override
        public boolean supports(Class<?> target) {
          return Plugin.class.isAssignableFrom(target);
        }
      });
      return ret;
    }

    @Override
    protected <T> Converter<String[]> chooseConverter(Class<T> clazz, List<Converter<String[]>> from) {
      return from.get(0);
    }
  }

  public static class StringResolver extends Plugin.Param.Resolver<String> {
    public static StringResolver INSTANCE = new StringResolver();

    public StringResolver() {
      super(createBuiltInConverters());
    }

    private static List<Converter<String>> createBuiltInConverters() {
      List<Converter<String>> ret = new ArrayList<Converter<String>>();
      ////
      // Primitives
      ret.add(new Converter.Simple<String>(boolean.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2boolean(in);
        }
      });
      ret.add(new Converter.Simple<String>(byte.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2byte(in);
        }
      });
      ret.add(new Converter.Simple<String>(char.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2char(in);
        }
      });
      ret.add(new Converter.Simple<String>(short.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2short(in);
        }
      });
      ret.add(new Converter.Simple<String>(int.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2int(in);
        }
      });
      ret.add(new Converter.Simple<String>(long.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2long(in);
        }
      });
      ret.add(new Converter.Simple<String>(float.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2float(in);
        }
      });
      ret.add(new Converter.Simple<String>(double.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2double(in);
        }
      });
      ret.add(new Converter.Simple<String>(Class.class) {
        @Override
        protected Object convert(String in) {
          return PluginUtils.str2Class(in);
        }
      });
      ////
      // Wrappers
      ret.add(new Converter.Simple<String>(Boolean.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2boolean(in);
        }
      });
      ret.add(new Converter.Simple<String>(Byte.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2byte(in);
        }
      });
      ret.add(new Converter.Simple<String>(Character.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2char(in);
        }
      });
      ret.add(new Converter.Simple<String>(Short.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2short(in);
        }
      });
      ret.add(new Converter.Simple<String>(Integer.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2int(in);
        }
      });
      ret.add(new Converter.Simple<String>(Long.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2long(in);
        }
      });
      ret.add(new Converter.Simple<String>(Float.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2float(in);
        }
      });
      ret.add(new Converter.Simple<String>(Double.class) {
        @Override
        protected Object convert(String in) {
          if (in == null)
            return null;
          return PluginUtils.str2double(in);
        }
      });
      ////
      // Common objects
      // String
      ret.add(new Converter.Simple<String>(String.class) {
        @Override
        protected Object convert(String in) {
          return in;
        }
      });
      // String
      ret.add(new Converter.Simple<String>(Enum.class) {
        @Override
        protected Object convert(String in) {
          return in;
        }
      });
      // Enum
      ret.add(new Converter<String>() {
        @Override
        public Object convert(Class requested, String in) {
          //noinspection unchecked
          return CompatUtils.cast(requested, Enum.valueOf((Class<Enum>) requested, in));
        }

        @Override
        public boolean supports(Class<?> target) {
          ////
          // If it is an enum, let runner try to proceed. If the enum isn't compatible,
          // you will see an exception later, either way.
          return target.isEnum();
        }
      });
      return ret;
    }

    @Override
    protected <T> Converter<String> chooseConverter(Class<T> clazz, List<Converter<String>> from) {
      return from.get(0);
    }
  }
}
