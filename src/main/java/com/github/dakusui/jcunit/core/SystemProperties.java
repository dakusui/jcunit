package com.github.dakusui.jcunit.core;

import java.io.File;

/**
 * A singleton class to access system properties from inside JCUnit.
 * JCUnit code shouldn't access any system property without using this class.
 * 
 * @author hiroshi
  */
public class SystemProperties {
	private SystemProperties() {}
	
	public static File jcunitBaseDir() {
		File ret;
		String rec = System.getProperty("jcunit.basedir");
		if (rec != null) {
			ret = new File(rec);
		} else {
			ret = new File(".jcunit");
		}
		return ret;
	}
}
