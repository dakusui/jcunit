package com.github.dakusui.jcunit.core;

import java.io.File;

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
    };

    public abstract String key();
    }

  private SystemProperties() {
  }

  public static File jcunitBaseDir() {
    File ret;
    String rec = System.getProperty(KEY.BASEDIR.key());
    if (rec != null) {
      ret = new File(rec);
    } else {
      ret = new File(".jcunit");
    }
    return ret;
  }

  public static boolean isDebugEnabled() {
    return Boolean.parseBoolean(System.getProperty(KEY.DEBUG.key(), "false"));
  }

  public static boolean isRecorderEnabled() {
    return Boolean.parseBoolean(System.getProperty(KEY.RECORDER.key(), "false"));
  }

  public static boolean isReplayerEnabled() {
    return Boolean.parseBoolean(System.getProperty(KEY.REPLAYER.key(), "false"));
  }

}
