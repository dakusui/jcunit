package com.github.dakusui.jcunit.core;

import java.io.File;

import static java.lang.System.*;

/**
 * A singleton class to access system properties from inside JCUnit. JCUnit code
 * shouldn't access any system property without using this class.
 *
 * @author hiroshi
 */
public class SystemProperties {
  public static enum KEY {
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
      @Override public String key() {
        return "jcunit.recorder";
      }
    },
    REPLAYER {
      @Override public String key() {
        return "jcunit.replayer";
      }
    },
    RANDOMSEED {
      @Override public String key() { return "jcunit.generator.randomseed"; }
    }
    ;

    public abstract String key();
    }

  private SystemProperties() {
  }

  public static File jcunitBaseDir() {
    File ret;
    String rec = getProperty(KEY.BASEDIR.key());
    if (rec != null) {
      ret = new File(rec);
    } else {
      ret = new File(".jcunit");
    }
    return ret;
  }

  public static boolean isDebugEnabled() {
    return Boolean.parseBoolean(getProperty(KEY.DEBUG.key(), "false"));
  }

  public static boolean isRecorderEnabled() {
    return Boolean.parseBoolean(getProperty(KEY.RECORDER.key(), "false"));
  }

  public static boolean isReplayerEnabled() {
    return Boolean.parseBoolean(getProperty(KEY.REPLAYER.key(), "false"));
  }

  public static long randomSeed() {
    String randomSeedKey = KEY.RANDOMSEED.key();
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


}
