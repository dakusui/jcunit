package com.github.dakusui.jcunit8.examples.config;


import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

@RunWith(JCUnit8.class)
public class ConfigExample {
  @ParameterSource
  public Parameter.Factory<String> platform() {
    return ParameterUtils.simple("Linux", "MacOSX", "Windows");
  }

  @ParameterSource
  public Parameter.Factory<String> java() {
    return ParameterUtils.simple("JavaSE7", "JavaSE8", "OpenJDK7");
  }

  @ParameterSource
  public Parameter.Factory<String> browser() {
    return ParameterUtils.simple("Safari", "Firefox", "Chrome", "InternetExplorer");
  }

  @ParameterSource
  public Parameter.Factory<String> dbms() {
    return ParameterUtils.simple("PostgreSQL", "MySQL", "SQLServer");
  }

  @ParameterSource
  public Parameter.Factory<String> applicationServer() {
    return ParameterUtils.simple("Jetty", "Tomcat");
  }

  @ParameterSource
  public Parameter.Factory<String> webServer() {
    return ParameterUtils.simple("Apache HTTP server", "IIS");
  }

  @Test
  public void print(
      @From("platform") String platform,
      @From("java") String java,
      @From("browser") String browser,
      @From("dbms") String dbms,
      @From("applicationServer") String applicationServer,
      @From("webServer") String webServer
  ) {
    System.out.printf("%s %s %s %s %s %s%n", platform, java, browser, dbms, applicationServer, webServer);
  }
}
