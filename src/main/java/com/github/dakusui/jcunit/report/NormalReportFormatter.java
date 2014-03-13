package com.github.dakusui.jcunit.report;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.IdentityHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.junit.runner.Description;

import com.github.dakusui.jcunit.core.BasicSummarizer.ResultMatrix;
import com.github.dakusui.jcunit.core.RuleSet;
import com.github.dakusui.jcunit.core.RuleSet.Pair;
import com.github.dakusui.jcunit.core.RuleSet.RuleIgnored;
import com.github.dakusui.jcunit.generators.TestArrayGenerator;
import com.github.dakusui.jcunit.report.Reporter.Domain;
import com.github.dakusui.jcunit.report.Reporter.FieldSet;
import com.github.dakusui.lisj.Basic;

public class NormalReportFormatter implements ReportFormatter {
  private static final String  OTHERWISECLAUSE_FORMAT = "(otherwise-%d)";

  private Map<Object, Integer> idMap                  = new IdentityHashMap<Object, Integer>();
  private Map<Object, Integer> levelMap               = new HashMap<Object, Integer>();
  private Set<Integer>         leaves                 = new HashSet<Integer>();

  private char                 domainKeycode;

  public NormalReportFormatter() {
  }

  @Override
  public void beginConditionMatrixSection(ReportWriter writer, Class<?> klazz) {
    writeLine(writer, klazz, "* MATRIX *");
  }

  @Override
  public void beginDomainsSection(ReportWriter writer, Class<?> klazz) {
    writeLine(writer, klazz, "* DOMAINS *");
    this.domainKeycode = 'A';
  }

  @Override
  public void beginTestClassFooter(ReportWriter writer, Class<?> klazz) {
    writeLine(writer, klazz, "***********************************************");
    writeLine(writer, klazz, "***                                         ***");
    writeLine(writer, klazz, "***          T E S T S U M M A R Y          ***");
    writeLine(writer, klazz, "***                                         ***");
    writeLine(writer, klazz, "***********************************************");
    writeLine(writer, klazz, "");
  }

  @Override
  public void beginTestClassHeader(ReportWriter writer, Class<?> klazz) {
    writeLine(writer, klazz, "***********************************************");
    writeLine(writer, klazz, "***                                         ***");
    writeLine(writer, klazz, "***          T E S T   M A T R I X          ***");
    writeLine(writer, klazz, "***                                         ***");
    writeLine(writer, klazz, "***********************************************");
    writeLine(writer, klazz, "");
  }

  @Override
  public void endConditionMatrixSection(ReportWriter writer, Class<?> klazz) {
  }

  @Override
  public void endDomainsSection(ReportWriter writer, Class<?> klazz) {
  }

  @Override
  public void endTestClassFooter(ReportWriter writer, Class<?> klazz) {
  }

  @Override
  public void endTestClassHeader(ReportWriter writer, Class<?> klazz) {
  }

  @Override
  public void formatConditionMatrix(ReportWriter writer, Class<?> klazz,
      TestArrayGenerator<Field, Object> testArrayGenerator) {
    writer.writeLine(klazz, 0, "* MATRIX *");
    // //
    // print out the header
    String header = String.format("%22s", "");
    int numKeys = testArrayGenerator.getKeys().size();
    boolean firstTime = true;
    char keyCode = 'A';
    for (int i = 0; i < numKeys; i++) {
      if (firstTime)
        firstTime = false;
      else
        header += ",";
      header += String.format("%-2s", keyCode);
      keyCode++;
    }
    writer.writeLine(klazz, 0, header);
    // //
    // print out matrix body.
    long size = testArrayGenerator.size();
    for (int i = 0; i < size; i++) {
      String line = String.format("%-20s", String.format("testrun[%d]:", i));
      firstTime = true;
      for (Field key : testArrayGenerator.getKeys()) {
        int valueCode = testArrayGenerator.getIndex(key, i);
        if (firstTime)
          firstTime = false;
        else
          line += ",";
        line += String.format("%02d", valueCode);
      }
      writer.writeLine(klazz, 1, line);
    }
    writer.writeLine(klazz, 0, "");
  }

  @Override
  public void formatResult(ReportWriter writer, Description desc, boolean ok) {
    writer.writeLine(desc, 0, "* RESULT *");
    writer.writeLine(desc, 1, ok ? "PASSED" : "FAILED");
    writer.writeLine(desc, 0, "");
  }

  @Override
  public void formatResultMatrix(ReportWriter writer, Class<?> klazz,
      RuleSet ruleSet, ResultMatrix matrix) {
    writer.writeLine(klazz, 0, "* TEST RESULT MATRIX *");
    int registeredIds = ruleSet.registeredIds();
    String[] headers = new String[ruleSet.maxLevel() + 1];
    for (int i = 0; i < headers.length; i++) {
      headers[i] = String.format("     %-30s", "LEVEL " + i + " PREDICATE");
    }
    for (int j = 0; j < registeredIds; j++) {
      for (int i = 0; i < headers.length; i++) {
        if (i == ruleSet.levelOf(j))
          headers[i] += String.format("%02d ", j);
        else
          headers[i] += "   ";
      }
    }
    for (String h : headers)
      writer.writeLine(klazz, 0, h);
    String line;
    for (String testName : matrix.testNames()) {
      line = String.format("[%-3s]%-30s", matrix.getResultType(testName),
          testName);
      for (int objId = 0; objId < registeredIds; objId++) {
        String f;
        if (matrix.isError(testName, objId)) {
          f = "E";
        } else {
          if (matrix.hasEntry(testName, objId)) {
            f = matrix.get(testName, objId) ? "T" : "F";
          } else {
            f = "-";
          }
        }
        if (ruleSet.isLeaf(objId) && !"-".equals(f) && !"T".equals(f)) {
          line += String.format("<%s>", f);
        } else {
          line += String.format(" %s ", f);
        }
      }
      writer.writeLine(klazz, 0, line);
    }
    writer.writeLine(klazz, 0, "");
  }

  @Override
  public void formatRuleSet(ReportWriter writer, Description desc,
      RuleSet ruleSet) {
    this.identifyObjects(ruleSet, 0, 0);
    writer.writeLine(desc, 0, "* RULES *");
    try {
      this.apply(writer, desc, ruleSet, 0);
    } catch (RuleIgnored e) {
      // //
      // Even if an ignored rule is found, it's just ignore in formatting
      // process. Because it is just printing process.
    } finally {
      writer.writeLine(desc, 0, "");
    }
  }

  private boolean apply(ReportWriter writer, Description desc, RuleSet ruleSet,
      int _indentLevel) throws RuleIgnored {
    boolean passed = true;
    boolean matchedAtLeastOnce = false;

    int indent;
    for (Pair cur : ruleSet.rulePairs()) {
      // //
      // reset the indentation level.
      indent = _indentLevel;
      if (check(writer, desc, indent, cur.cond(), cur.condResult())) {
        indent = indent + 1;
        matchedAtLeastOnce = true;
        Object nested = cur.nested();
        if (nested instanceof RuleSet) {
          RuleSet nestedRuleSet = (RuleSet) nested;
          try {
            passed &= this.apply(writer, desc, nestedRuleSet, indent + 1);
            if (cur.cut())
              break;
          } catch (RuleIgnored e) {
            passed = false;
            if (e.source() == nested)
              continue;
          }
        } else {
          passed &= this.expect(writer, desc, indent, nested,
              cur.nestedResult());
          if (cur.cut())
            break;
        }
      }
    }
    // /
    // reset the indentation level.
    indent = _indentLevel;
    if (matchedAtLeastOnce) {
      return passed;
    } else {
      Pair otherwise = ruleSet.otherwise();
      if (otherwise != null) {
        Object cond = otherwise.cond();
        if (check(writer, desc, indent, cond, true)) {
          indent++;
        }
        Object nested = otherwise.nested();
        if (nested instanceof RuleSet) {
          RuleSet nestedRuleSet = (RuleSet) nested;
          return this.apply(writer, desc, nestedRuleSet, indent);
        } else if (nested != null) {
          return expect(writer, desc, indent, nested, otherwise.nestedResult());
        }
      }
    }
    throw new RuleIgnored(ruleSet);
  }

  private boolean check(ReportWriter writer, Description desc, int indent,
      Object cond, boolean result) {
    String id = String.format("[%02d]", idOf(cond));
    // Summarizer s = RuleSet.this.summarizer;
    if (result) {
      // s.passed(testName, idOf(cond));
      writer.writeLine(desc, indent, id + "MATCHED:" + Basic.tostr(cond));
    } else {
      // s.failed(testName, idOf(cond));
      writer.writeLine(desc, indent, id + "NOT MATCHED:" + Basic.tostr(cond));
    }
    return result;
  }

  private boolean expect(ReportWriter writer, Description desc, int indent,
      Object nested, boolean result) {
    String id = String.format("[%02d]", idOf(nested));
    // Summarizer s = RuleSet.this.summarizer;
    if (result) {
      // s.passed(testName, idOf(nested));
      writer.writeLine(desc, indent, id + "PASS:" + Basic.tostr(nested));
    } else {
      // s.failed(testName, idOf(nested));
      writer.writeLine(desc, indent, id + "FAIL:" + Basic.tostr(nested));
    }
    return result;
  }

  @Override
  public void formatRulesResult(ReportWriter writer, Class<?> klazz) {
  }

  @Override
  public void formatValues(ReportWriter writer, Description desc,
      FieldSet fields) {
    writer.writeLine(desc, 0,
        String.format("* %s VALUES *", fields.categoryName()));
    writer.writeLine(desc, 1, String.format("VALUES(%d)", fields.size()));
    List<Field> keys = new ArrayList<Field>(fields.keySet());
    Collections.sort(keys, new Comparator<Field>() {
      @Override
      public int compare(Field o1, Field o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    for (Field key : keys) {
      Object v = fields.get(key);
      writer.writeLine(desc, 2, String.format("%s:%s(%s)", key.getName(),
          v == null ? null : ArrayUtils.toString(v), key.getType().getName()));
    }
    this.formatExceptions(writer, desc, fields);
  }

  private void formatExceptions(ReportWriter writer, Description desc,
      FieldSet fields) {
    writer.writeLine(desc, 1, "* EXCEPTIONS *");
    List<Field> keys = new ArrayList<Field>(fields.keySet());
    Collections.sort(keys, new Comparator<Field>() {
      @Override
      public int compare(Field o1, Field o2) {
        return o1.getName().compareTo(o2.getName());
      }
    });
    boolean atLeastOneException = false;
    for (Field key : keys) {
      Object v = fields.get(key);
      if (v instanceof Throwable) {
        Throwable t = (Throwable) v;
        writer.writeLine(desc, 2, String.format("%s:%s(%s)", key.getName(), v,
            key.getType().getName()));
        writer.writeLine(desc, 3, t.getMessage());
        for (StackTraceElement ste : t.getStackTrace()) {
          writer.writeLine(desc, 4, ste.toString());
        }
        writer.writeLine(desc, 2, "");
        atLeastOneException = true;
      }
    }
    if (!atLeastOneException) {
      writer.writeLine(desc, 2, "(none)");
    }
  }

  @Override
  public void formatDomain(ReportWriter writer, Class<?> klazz, Domain domain) {
    // //
    // print out header
    Field key = domain.field();
    String domainHeader = String.format("%s:%s(%s)", this.domainKeycode,
        key.getName(), key.getType());
    writer.writeLine(klazz, 1, domainHeader);
    Object[] d = domain.values();
    for (int i = 0; i < d.length; i++) {
      String l = String.format("%02d:'%s'", i, Basic.tostr(d[i]));
      writer.writeLine(klazz, 2, l);
    }
    this.domainKeycode++;
  }

  @Override
  public void beginTestCase(ReportWriter writer, Description desc) {
    writer
        .writeLine(desc, 0, "***********************************************");
    writer
        .writeLine(desc, 0, "***                                         ***");
    writer
        .writeLine(desc, 0, "***           T E S T R E P O R T           ***");
    writer
        .writeLine(desc, 0, "***                                         ***");
    writer
        .writeLine(desc, 0, "***********************************************");
    writer.writeLine(desc, 0, "");

    writer.writeLine(desc, 0, "* TEST NAME *");
    writer.writeLine(desc, 1,
        String.format("'%s/%s'", desc.getClassName(), desc.getMethodName()));
    writer.writeLine(desc, 0, "");
  }

  @Override
  public void endTestCase(ReportWriter writer, Description desc) {
  }

  private void writeLine(ReportWriter writer, Class<?> klazz, String s,
      Object... params) {
    writer.writeLine(klazz, 0,
        String.format(s.replaceAll("\\{\\}", "%s"), params));
  }

  /**
   * Assign identifiers to objects in <code>RuleSet</code>. Since JCUnit
   * framework instantiates <code>RuleSet</code> object every time a test case
   * is executed, it needs to identify which object corresponding to which
   * object in another run. JCUnit figures out this based on the fact that the
   * rule objects are instantiated in the same order for each run.
   * 
   * @param ruleSet
   *          A <code>RuleSet</code> object
   * @param nextIdTobeUsed
   *          The id to be used the next time.
   * @param level
   *          the current level of the object.
   * @return The next id number to be passed to this method.
   */
  private int identifyObjects(RuleSet ruleSet, int nextIdTobeUsed, int level) {
    List<Pair> rules = new LinkedList<Pair>();
    rules.addAll(ruleSet.rulePairs());
    Pair otherwise = ruleSet.otherwise();
    if (otherwise != null)
      rules.add(otherwise);
    for (Pair p : ruleSet.rulePairs()) {
      Object cond = p.cond();
      if (OTHERWISECLAUSE_FORMAT.equals(cond)) {
        p.cond(String.format(p.cond().toString(), nextIdTobeUsed));
      }
      levelMap.put(cond, level);
      idMap.put(cond, nextIdTobeUsed++);
      Object nested = p.nested();
      if (nested instanceof RuleSet) {
        nextIdTobeUsed = identifyObjects((RuleSet) nested, nextIdTobeUsed,
            level + 1);
      } else {
        levelMap.put(nested, level + 1);
        leaves.add(nextIdTobeUsed);
        idMap.put(nested, nextIdTobeUsed++);
      }
    }
    return nextIdTobeUsed;
  }

  /*
   * Returns an id of the object. Before using this method,
   * <code>identifyObjects</code> must be invoked beforehand on this object.
   */
  private int idOf(Object obj) {
    if (!idMap.containsKey(obj)) {
      assert false;
      return -1;
    }
    return idMap.get(obj);
  }
}
