package com.github.dakusui.jcunit.core.utils;

import java.io.File;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.getProperty;

/**
 * A singleton class to access system properties from inside JCUnit. JCUnit code
 * shouldn't access any system property without using this class.
 *
 * @author hiroshi
 */
public class SystemProperties {
  public enum Key {
    DEBUG {
      @Override
      public String key() {
        return "jcunit.debug";
      }
    },
    BASEDIR {
      @Override
      public String key() {
        return "jcunit.basedir";
      }
    },
    RECORDER {
      @Override
      public String key() {
        return "jcunit.recorder";
      }
    },
    REPLAYER {
      @Override
      public String key() {
        return "jcunit.replayer";
      }
    },
    /**
     * This system property is used  by {@code RandomCoveringArrayEngine}.
     */
    RANDOMSEED {
      @Override
      public String key() {
        return "jcunit.generator.randomseed";
      }
    },
    REUSETESTSUITE {
      @Override
      public String key() {
        return "jcunit.reusetestsuite";
      }
    },
    /**
     * Should be used only by {@code PlugIn.Param}.
     */
    DUMMY {
      @Override
      public String key() {
        return null;
      }
    };

    public abstract String key();
  }

  private SystemProperties() {
  }

  public static String get(Key propertyKey) {
    return get(propertyKey, null);
  }

  public static String get(Key key, String s) {
    return getProperty(Checks.checknotnull(key).key(), s);
  }


  public static File jcunitBaseDir() {
    File ret;
    String rec = getProperty(Key.BASEDIR.key());
    if (rec != null) {
      ret = new File(rec);
    } else {
      ret = new File(".jcunit");
    }
    return ret;
  }

  public static boolean isDebugEnabled() {
    return Boolean.parseBoolean(getProperty(Key.DEBUG.key(), "false"));
  }

  public static boolean isRecorderEnabled() {
    return Boolean.parseBoolean(getProperty(Key.RECORDER.key(), "false"));
  }

  public static boolean isReplayerEnabled() {
    return Boolean.parseBoolean(getProperty(Key.REPLAYER.key(), "false"));
  }

  public static long randomSeed() {
    String randomSeedKey = Key.RANDOMSEED.key();
    synchronized (SystemProperties.class) {
      try {
        if (getProperty(randomSeedKey) == null) {
          System.setProperty(randomSeedKey, Long.toString(currentTimeMillis()));
        }
        return Long.parseLong(
            getProperty(randomSeedKey)
        );
      } catch (NumberFormatException e) {
        Checks.checkenv(
            false,
            "The value '%s' specified for system property '%s' is not an integer.",
            getProperty(randomSeedKey),
            randomSeedKey
        );
      }
    }
    ////
    // This path will not be executed.
    throw new RuntimeException();
  }

  public static boolean reuseTestSuite() {
    return Boolean.parseBoolean(System.getProperty(Key.REUSETESTSUITE.key()));
  }
}
