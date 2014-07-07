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
    BASEDIR {
      @Override
      public String key() {
        return "jcunit.basedir";
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

  public static void jcunitBaseDir(String basedir) {
    System.setProperty("jcunit.basedir", basedir);
  }
}
