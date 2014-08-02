package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.ConfigUtils;
import com.github.dakusui.jcunit.core.ParamType;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.rules.JCUnitRecorder;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class RecordedTuplePlayer extends TupleGeneratorBase {
  private SortedMap<Long, Tuple> tuples;

  public static enum ReplayMode {
    All {
      @Override public boolean shouldBeReplayed(File testStoreDir) {
        return true;
      }
    },
    FailedOnly {
      @Override public boolean shouldBeReplayed(File testStoreDir) {
        return new File(testStoreDir, JCUnitRecorder.EXCEPTION_FILENAME)
            .exists();
      }
    };

    public abstract boolean shouldBeReplayed(File testStoreDir);
  }

  private static interface FoundTupleObserver {
    void found(File f);
  }

  @Override
  public Tuple getSchemafulTuple(int tupleId) {
    Utils.checkcond(this.tuples.containsKey((long) tupleId));
    return this.tuples.get((long) tupleId);
  }

  @Override
  protected long initializeSchemafulTuples(Object[] params) {
    File baseDir = Utils.baseDirFor((String)params[1], getTargetClass());
    final int[] work = new int[] { 0 };
    File[] tupleDirs = getRecordedTupleDirectories((ReplayMode) params[0],
        baseDir, new FoundTupleObserver() {
          @Override public void found(File f) {
            work[0]++;
          }
        });
    int numFoundTuples = work[0];
    ConfigUtils.checkEnv(tupleDirs != null,
        "Test hasn't been run with 'JCUnitRecorder' rule yet. No tuple containing directory under '%s' was found.",
        baseDir
        );
    assert tupleDirs != null;
    this.tuples = new TreeMap<Long, Tuple>();
    try {
      for (File dir : tupleDirs) {
        Tuple tuple = TupleUtils.load(
            Utils.openForRead(new File(dir, JCUnitRecorder.TESTCASE_FILENAME)));
        tuples.put(getIdFromDirName(dir.getName()), tuple);
      }
    } catch (JCUnitException e) {
      JCUnitException ee = new JCUnitEnvironmentException(e);
      ee.setStackTrace(e.getStackTrace());
      throw ee;
    }
    ////
    // Returning number of total recorded test cases.
    return numFoundTuples;
  }

  private long getIdFromDirName(String dirName) {
    return Long.parseLong(dirName.substring(dirName.lastIndexOf('-') + 1));
  }

  @Override
  public long nextId(long tupleId) {
    Utils.checkcond(this.tuples.containsKey(tupleId));
    Iterator<Long> tail = this.tuples.tailMap(tupleId).keySet().iterator();
    Utils.checkcond(tail.hasNext());
    tail.next();
    if (!tail.hasNext()) {
      return -1;
    }
    return tail.next();
  }

  @Override
  public long firstId() {
    if (this.tuples.size() == 0) {
      return -1;
    }
    return this.tuples.firstKey();
  }

  private File[] getRecordedTupleDirectories(final ReplayMode mode,
      File baseDir,
      final FoundTupleObserver obs
  ) {
    return baseDir.listFiles(new FilenameFilter() {
                               @Override
                               public boolean accept(File dir, String s) {
                                 String prefix = "test-";
                                 if (!s.startsWith(prefix)) {
                                   return false;
                                 }
                                 if (!new File(dir, s).isDirectory()) {
                                   return false;
                                 }
                                 try {
                                   int i = Integer
                                       .parseInt(s.substring(prefix.length()));
                                   if (i < 0) return false;
                                 } catch (NumberFormatException e) {
                                   return false;
                                 }
                                 File f = new File(dir, s);
                                 obs.found(f);
                                 if (!mode.shouldBeReplayed(f)) {
                                   return false;
                                 }
                                 return true;
                               }
                             }
    );
  }

  @Override public ParamType[] parameterTypes() {
    return new ParamType[] {
        new ParamType.NonArrayType() {
          @Override protected Object parse(String str) {
            return ReplayMode.valueOf(str);
          }

          @Override public String toString() {
            return RecordedTuplePlayer.class.getCanonicalName()
                + ".ReplayMode";
          }
        }.withDefaultValue(ReplayMode.All),
        ParamType.String.withDefaultValue(null)
    };
  }
}
