package com.github.dakusui.jcunit.report;

import org.junit.runner.Description;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.dakusui.jcunit.core.SystemProperties;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

public class ReportWriter {
  /**
   * A file name to write a report.
   */
  private static final String REPORT_FILENAME = "report.jcunit";

  /**
   * A logger object.
   */
  private static final Logger LOGGER          = LoggerFactory
                                                  .getLogger(ReportWriter.class);

  /**
   * Creates an object of this class.
   */
  public ReportWriter() {
  }

  /**
   * Deletes a test case level report file.
   * 
   * @param desc
   *          A description object to locate a report file.
   */
  public void deleteReport(Description desc) {
    fileToWrite(desc.getClassName(), desc.getMethodName()).delete();
  }

  /**
   * Deletes a test class level report file.
   * 
   * @param klazz
   *          A class object to locate a report file.
   */
  public void deleteReport(Class<?> klazz) {
    fileToWrite(klazz.getCanonicalName()).delete();
  }

  /**
   * Writes a line for 'test case level' report.
   * 
   * @param indentLevel
   *          indent level.
   * @param str
   *          A string to be printed.
   */
  public void writeLine(Description desc, int indentLevel, String str) {
    String s = indent(indentLevel) + str;
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(s);
    }
    writeLineToFile(fileToWrite(desc.getClassName(), desc.getMethodName()), s);

  }

  public void writeErrorLine(Description desc, int indentLevel, String str) {
    String s = indent(indentLevel) + str;
    if (LOGGER.isErrorEnabled()) {
      LOGGER.error(s);
    }
    writeLineToFile(fileToWrite(desc.getClassName(), desc.getMethodName()), s);
  }

  public void writeLine(Class<?> klazz, int indentLevel, String str) {
    String s = indent(indentLevel) + str;
    if (LOGGER.isInfoEnabled()) {
      LOGGER.info(s);
    }
    writeLineToFile(fileToWrite(klazz.getCanonicalName()), s);
  }

  /*
   * Writes a given line to a file.
   */
  private void writeLineToFile(File f, String line) {
    try {
      // //
      // Creates a necessary directory, if it doesn't exist.
      File p = f.getParentFile();
      if (!p.exists())
        p.mkdirs();

      f.getParentFile().mkdirs();
      FileWriter fw = new FileWriter(f, true);
      try {
        BufferedWriter bw = new BufferedWriter(fw);
        try {
          PrintWriter pw = new PrintWriter(bw);
          try {
            pw.println(line);
            pw.flush();
          } finally {
            pw.close();
          }
        } finally {
          bw.close();
        }
      } finally {
        fw.close();
      }
    } catch (IOException e) {
      String msg = String.format(
          "!!! Failed to write a line to file: '%s' (%s)", f, e.getMessage());
      LOGGER.error(msg);
      throw new RuntimeException(msg, e);
    }
  }

  private File fileToWrite(String className, String methodName) {
    File ret = new File(new File(dirToWrite(className), methodName),
        REPORT_FILENAME);
    return ret;
  }

  private File fileToWrite(String className) {
    File ret = new File(new File(SystemProperties.jcunitBaseDir(), className),
        REPORT_FILENAME);
    return ret;
  }

  private File dirToWrite(String className) {
    File ret = new File(SystemProperties.jcunitBaseDir(), className);
    return ret;
  }

  private String indent(int indentLevel) {
    String indent = "";
    for (int i = 0; i < indentLevel; i++) {
      indent += "  ";
    }
    return indent;
  }
}