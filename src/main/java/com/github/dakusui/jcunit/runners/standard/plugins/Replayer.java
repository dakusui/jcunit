package com.github.dakusui.jcunit.runners.standard.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.plugins.caengines.CAEngine;
import com.github.dakusui.jcunit.plugins.caengines.CAEngineBase;
import com.github.dakusui.jcunit.runners.standard.rules.Recorder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class Replayer extends CAEngineBase {
  private final GenerationMode         generationMode;
  private final String                 dataDir;
  private final ReplayMode             replayMode;
  private       SortedMap<Long, Tuple> tuples;
  private       CAEngine               fallbackGenerator;

  /**
   * Creates an object of this class.
   * <p/>
   * Parameters in former versions:
   * Below is the list of parameters.
   * <ul>
   * <li>0: Replay mode. 'All' or 'FailedOnly'. Note that 'FailedOnly' is only effective for
   * generated test cases. So, test cases returned by '@CustomTestCase' annotated methods or
   * {@code ConstraintManager#getViolations} will be executed regardless of this value. Remove
   * the annotation or make the methods return empty lists to suppress them.</li>
   * <li>1: Base directory of test data. By default, null (, which then defaults to .jcunit).</li>
   * <li>2: Class name of a fall back tuple generator. By default IPO2CAEngine.</li>
   * <li>3...: Parameters passed to fallback tuple generator.</li>
   * </ul>
   *
   * @param caEngine A tuple generator.
   * @param generationMode Generation mode. Determines whether let the tuple generator generate a
   *                       new test suite or load it from file system. ("Fallback", "Replay")
   * @param replayMode     Determines if all the test cases or only failed ones in the last run.
   *                       You need to use this is "recorder" rule with this class. ("All", "FailedOnly")
   * @param dataDirName    A directory to store execution data of this class.
   */
  public Replayer(
      @Param(source = Param.Source.INSTANCE,
          defaultValue = { "com.github.dakusui.jcunit.plugins.caengines.IPO2CAEngine", "2" })
      CAEngine caEngine,
      @Param(source = Param.Source.INSTANCE,
          defaultValue = { "Fallback" })
      GenerationMode generationMode,
      @Param(source = Param.Source.INSTANCE,
          defaultValue = { "All" })
      ReplayMode replayMode,
      @Param(source = Param.Source.SYSTEM_PROPERTY,
          propertyKey = SystemProperties.KEY.BASEDIR,
          defaultValue = { ".jcunit" })
      String dataDirName
  ) {
    this.generationMode = Checks.checknotnull(generationMode);
    this.replayMode = Checks.checknotnull(replayMode);
    this.fallbackGenerator = Checks.checknotnull(caEngine);
    this.dataDir = Checks.checknotnull(dataDirName);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public Tuple getTuple(int tupleId) {
    return this.generationMode.getTuple(this, tupleId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  protected long initializeTuples() {
    return this.generationMode.initializeTuples(this);
  }

  private long getIdFromDirName(String dirName) {
    return Long.parseLong(dirName.substring(dirName.lastIndexOf('-') + 1));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long nextId(long tupleId) {
    return this.generationMode.nextId(this, tupleId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long firstId() {
    return this.generationMode.firstId(this);
  }

  private File[] getRecordedTupleDirectories(final ReplayMode mode,
      File baseDir,
      final FoundTupleObserver obs
  ) {
    return baseDir.listFiles(new FilenameFilter() {
                               @Override
                               public boolean accept(File dir, String s) {
                                 String prefix = "data-";
                                 if (!s.startsWith(prefix)) {
                                   return false;
                                 }
                                 if (!new File(dir, s).isDirectory()) {
                                   return false;
                                 }
                                 try {
                                   int i = Integer
                                       .parseInt(s.substring(prefix.length()));
                                   if (i < 0) {
                                     return false;
                                   }
                                 } catch (NumberFormatException e) {
                                   return false;
                                 }
                                 File f = new File(dir, s);
                                 obs.found(f);
                                 return mode.shouldBeReplayed(f);
                               }
                             }
    );
  }


  public enum GenerationMode {
    Replay {
      @Override
      long initializeTuples(
          Replayer tupleReplayer) {
        File baseDir = Recorder
            .testClassDataDirFor(tupleReplayer.dataDir,
                tupleReplayer.getTargetClass());
        final int[] work = new int[] { 0 };
        File[] tupleDirs = tupleReplayer
            .getRecordedTupleDirectories(tupleReplayer.replayMode,
                baseDir, new FoundTupleObserver() {
                  @Override
                  public void found(File f) {
                    work[0]++;
                  }
                });
        int numFoundTuples = work[0];
        Checks.checktest(tupleDirs != null,
            "Test hasn't been run with 'JCUnitRecorder' rule yet. No tuple containing directory under '%s' was found.",
            baseDir
        );
        tupleReplayer.tuples = new TreeMap<Long, Tuple>();
        for (File dir : tupleDirs) {
          Tuple tuple = TupleUtils.load(
              IOUtils.openForRead(
                  new File(dir, Recorder.TESTCASE_FILENAME)));
          tupleReplayer.tuples
              .put(tupleReplayer.getIdFromDirName(dir.getName()), tuple);
        }
        ////
        // Returning number of total recorded test cases.
        return numFoundTuples;
      }

      @Override
      Tuple getTuple(Replayer tuplePlayer, int tupleId) {
        Checks.checkcond(tuplePlayer.tuples.containsKey((long) tupleId));
        return tuplePlayer.tuples.get((long) tupleId);
      }

      @Override
      long nextId(Replayer tuplePlayer, long tupleId) {
        Checks.checkcond(tuplePlayer.tuples.containsKey(tupleId));
        Iterator<Long> tail = tuplePlayer.tuples.tailMap(tupleId).keySet()
            .iterator();
        Checks.checkcond(tail.hasNext());
        tail.next();
        if (!tail.hasNext()) {
          return -1;
        }
        return tail.next();
      }

      @Override
      long firstId(Replayer tuplePlayer) {
        if (tuplePlayer.tuples.size() == 0) {
          return -1;
        }
        return tuplePlayer.tuples.firstKey();
      }
    },
    Fallback {
      @SuppressWarnings("unchecked")
      @Override
      long initializeTuples(Replayer tupleReplayer) {
        ////
        // Wire
        CAEngine generator = tupleReplayer.fallbackGenerator;
        generator.setFactors(tupleReplayer.getFactors());
        generator.setConstraint(tupleReplayer.getConstraint());
        generator.setTargetClass(tupleReplayer.getTargetClass());
        generator.init();
        return generator.size();
      }

      @Override
      Tuple getTuple(Replayer tuplePlayer, int tupleId) {
        return tuplePlayer.fallbackGenerator.get(tupleId);
      }

      @Override
      long nextId(Replayer tuplePlayer, long tupleId) {
        return tuplePlayer.fallbackGenerator.nextId(tupleId);
      }

      @Override
      long firstId(Replayer tuplePlayer) {
        return tuplePlayer.fallbackGenerator.firstId();
      }
    };


    abstract long initializeTuples(Replayer tupleReplayer);

    abstract Tuple getTuple(Replayer tuplePlayer, int tupleId);

    abstract long nextId(Replayer tuplePlayer, long tupleId);

    abstract long firstId(Replayer tuplePlayer);
  }

  public enum ReplayMode {
    All {
      @Override
      public boolean shouldBeReplayed(File testStoreDir) {
        return true;
      }
    },
    FailedOnly {
      @Override
      public boolean shouldBeReplayed(File testStoreDir) {
        return new File(testStoreDir, Recorder.FAILED_FILENAME)
            .exists();
      }
    },;

    public abstract boolean shouldBeReplayed(File testStoreDir);
  }

  private interface FoundTupleObserver {
    void found(File f);
  }
}
