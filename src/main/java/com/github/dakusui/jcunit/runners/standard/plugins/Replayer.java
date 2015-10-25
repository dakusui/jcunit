package com.github.dakusui.jcunit.runners.standard.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.plugins.generators.TupleGenerator;
import com.github.dakusui.jcunit.plugins.generators.TupleGeneratorBase;
import com.github.dakusui.jcunit.runners.standard.annotations.Value;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class Replayer<S> extends TupleGeneratorBase {
  private final GenerationMode         generationMode;
  private       SortedMap<Long, Tuple> tuples;
  private       TupleGenerator         fallbackGenerator;

  /**
   * Creates an object of this class.
   */
  public Replayer(
      @Param(defaultValue = { "com.github.dakusui.jcunit.plugins.generators.IPO2TupleGenerator", "2" }) TupleGenerator tupleGenerator,
      @Param(defaultValue = { "Fallback" }) GenerationMode generationModeMode
  ) {
    this.generationMode = generationModeMode;
    this.fallbackGenerator = tupleGenerator;
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
    return this.generationMode.initializeTuples(this, new Object[] {} /* TODO IMPLEMENT THIS*/);
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
          Replayer tupleReplayer,
          Object[] params) {
        File baseDir = Recorder
            .testClassDataDirFor((String) params[1],
                tupleReplayer.getTargetClass());
        final int[] work = new int[] { 0 };
        File[] tupleDirs = tupleReplayer
            .getRecordedTupleDirectories((ReplayMode) params[0],
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
        return (Tuple) tuplePlayer.tuples.get((long) tupleId);
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
        return (Long) tuplePlayer.tuples.firstKey();
      }
    },
    Fallback {
      @SuppressWarnings("unchecked")
      @Override
      long initializeTuples(Replayer tupleReplayer, Object[] params) {
        ////
        // Extract parameters to be passed to fallbackGenerator.
        Value[] paramsToFallbackGenerator;
        if (params.length >= 4) {
          paramsToFallbackGenerator = Arrays
              .copyOfRange(params, 3, params.length, Value[].class);
        } else {
          paramsToFallbackGenerator = new Value[0];
        }
        ////
        // Wire
        TupleGenerator generator = tupleReplayer.fallbackGenerator;
        generator.setFactors(tupleReplayer.getFactors());
        generator.setConstraintManager(tupleReplayer.getConstraintManager());
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


    abstract long initializeTuples(Replayer tupleReplayer, Object[] args);

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
