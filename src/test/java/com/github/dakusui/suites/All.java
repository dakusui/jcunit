package com.github.dakusui.suites;

import org.junit.BeforeClass;
import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.reflections.Reflections;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;

import java.net.MalformedURLException;
import java.util.Set;

@RunWith(Suite.class)
@Suite.SuiteClasses({ ExampleSuite.class, UnitTests.class,
    CombinationTests.class })
public class All {
  @BeforeClass
  public static void loadAllTestClasses() throws MalformedURLException {
    Reflections reflections = new Reflections(new ConfigurationBuilder()
        .setUrls(ClasspathHelper.forPackage("com.github.dakusui")).setScanners(
            new SubTypesScanner(false)));
    Set<?> modules = reflections.getSubTypesOf(Object.class);
    System.out.printf("Loaded %d classes:\n", modules.size());
    for (Object klass : modules) {
      System.out.println("    " + klass);
    }
    System.out.println("----");
  }
}
