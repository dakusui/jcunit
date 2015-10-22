package com.github.dakusui.jcunit.examples.confg;

import com.github.dakusui.jcunit.annotations.FactorField;
import com.github.dakusui.jcunit.core.JCUnit;
import com.github.dakusui.jcunit.core.TestCaseUtils;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.ututils.Metatest;
import com.github.dakusui.jcunit.ututils.UTUtils;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit.class)
public class ConfigExample extends Metatest {
  public ConfigExample() {
    super(17, 0, 0);
  }

  @FactorField(stringLevels = {"Linux", "MacOSX", "Windows"})
  public String platform;
  @FactorField(stringLevels = {"JavaSE7", "JavaSE8", "OpenJDK7"})
  public String java;
  @FactorField(stringLevels = {"Safari", "Firefox", "Chrome", "InternetExplorer"})
  public String browser;
  @FactorField(stringLevels = {"PostgreSQL", "MySQL", "SQLServer"})
  public String dbms;
  @FactorField(stringLevels = {"Jetty", "Tomcat"})
  public String applicationserver;
  @FactorField(stringLevels = {"Apache HTTP server", "IIS"})
  public String webserver;

  @Before
  public void configureStdIOs() {
    UTUtils.configureStdIOs();
  }

  @Test
  public void print() {
    UTUtils.stdout().println(TupleUtils.toString(TestCaseUtils.toTestCase(this)));
  }
}
