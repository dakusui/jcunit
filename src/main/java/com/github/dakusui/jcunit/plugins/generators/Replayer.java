package com.github.dakusui.jcunit.plugins.generators;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.reflect.ReflectionUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitException;
import com.github.dakusui.jcunit.standardrunner.Recorder;
import com.github.dakusui.jcunit.standardrunner.annotations.Arg;

import java.io.File;
import java.io.FilenameFilter;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

public class Replayer<S> extends TupleGeneratorBase<S> {
  private final GenerationMode         generationMode;
  private final ReplayMode             replayMode;
  private final TupleGenerator         baseTupleGeneratorClass;
  private final S[]                    baseTupleGeneratorOptions;
  private       SortedMap<Long, Tuple> tuples;
  private       TupleGeneratorBase     fallbackGenerator;

  /**
   * Creates an object of this class.
   */
  public Replayer(
      @Param(source = Param.Source.RUNNER) Param.Translator<S> translator,
      @Param(defaultValue = "IPO2TupleGenerator") TupleGenerator tupleGenerator,
      // TODO: Review and fix
      @Param(source = Param.Source.SYSTEM_PROPERTY, propertyKey = SystemProperties.KEY.REPLAYER) GenerationMode generationModeMode,
      // TODO: Review and fix
      @Param(source = Param.Source.SYSTEM_PROPERTY, propertyKey = SystemProperties.KEY.REPLAYER) ReplayMode replayMode,
      @Param() S[] tupleGeneratorOptions
  ) {
    super(translator);
    this.generationMode = generationModeMode;
    this.replayMode = Checks.checknotnull(replayMode);
    this.baseTupleGeneratorClass = Checks.checknotnull(tupleGenerator);
    this.baseTupleGeneratorOptions = Checks.checknotnull(tupleGeneratorOptions);
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
    return this.generationMode.initializeTuples(this, new Object[] {} /* IMPLEMENT THIS*/);
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
        return (Long)tuplePlayer.tuples.firstKey();
      }
    },
    Fallback {
      @SuppressWarnings("unchecked")
      @Override
      long initializeTuples(Replayer tupleReplayer, Object[] params) {
        ////
        // Create fallbackGenerator instance.
        try {
          tupleReplayer.fallbackGenerator = ReflectionUtils.create((Class<? extends TupleGeneratorBase>) params[2]);
        } catch (JCUnitException e) {
          Checks.rethrowpluginerror(e.getCause(), e.getMessage());
        }
        ////
        // Extract parameters to be passed to fallbackGenerator.
        Arg[] paramsToFallbackGenerator;
        if (params.length >= 4) {
          paramsToFallbackGenerator = Arrays
              .copyOfRange(params, 3, params.length, Arg[].class);
        } else {
          paramsToFallbackGenerator = new Arg[0];
        }
        ////
        // Wire
        TupleGeneratorBase generator = tupleReplayer.fallbackGenerator;
        generator.setFactors(tupleReplayer.getFactors());
        generator.setConstraintManager(tupleReplayer.getConstraintManager());
        generator.setTargetClass(tupleReplayer.getTargetClass());
        generator.init();
        return generator.size();
      }

      @Override
      Tuple getTuple(Replayer tuplePlayer, int tupleId) {
        return tuplePlayer.fallbackGenerator.getTuple(tupleId);
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
