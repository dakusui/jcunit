package com.github.dakusui.jcunit.generators;

import com.github.dakusui.jcunit.core.*;
import com.github.dakusui.jcunit.core.rules.JCUnitRecorder;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.exceptions.JCUnitEnvironmentException;
import com.github.dakusui.jcunit.exceptions.JCUnitException;

import java.io.File;
import java.io.FilenameFilter;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Iterator;
import java.util.SortedMap;
import java.util.TreeMap;

/**
 * @see RecordedTuplePlayer#parameterTypes() for parameters definition.
 */
public class RecordedTuplePlayer extends TupleGeneratorBase {
  private final GenerationMode         generationMode;
  private       SortedMap<Long, Tuple> tuples;
  private       TupleGeneratorBase     fallbackGenerator;

  /**
   * Creates an object of this class.
   */
  public RecordedTuplePlayer() {
    this.generationMode = SystemProperties.isReplayerEnabled() ?
        GenerationMode.Replay :
        GenerationMode.Fallback;
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
  protected long initializeTuples(Object[] params) {
    return this.generationMode.initializeTuples(this, params);
  }

  private long getIdFromDirName(String dirName) {
    return Long.parseLong(dirName.substring(dirName.lastIndexOf('-') + 1));
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long nextId(long tupleId) {
    assert this.generationMode != null;
    return this.generationMode.nextId(this, tupleId);
  }

  /**
   * {@inheritDoc}
   */
  @Override
  public long firstId() {
    assert this.generationMode != null;
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

  /**
   * Returns definitions of the parameters for this class.
   * Below is the list of parameters.
   * <ul>
   * <li>0: Replay mode. 'All' or 'FailedOnly'. Note that 'FailedOnly' is only effective for
   * generated test cases. So, test cases returned by '@CustomTestCase' annotated methods or
   * {@code ConstraintManager#getViolations} will be executed regardless of this value. Remove
   * the annotation or make the methods return empty lists to suppress then.</li>
   * <li>1: Base directory of test data. By default, null (, which then defaults to .jcunit).</li>
   * <li>2: Class name of a fall back tuple generator. By default IPO2TupleGenerator.</li>
   * <li>3...: Parameters passed to fallback tuple generator.</li>
   * </ul>
   */
  @Override
  public ParamType[] parameterTypes() {
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
        ParamType.String.withDefaultValue(null),
        new ParamType.NonArrayType() {
          @SuppressWarnings("unchecked") @Override
          protected Class<? extends TupleGeneratorBase> parse(
              String str) {
            try {
              Class<?> ret = Class.forName(str);
              ConfigUtils.checkEnv(
                  TupleGeneratorBase.class.isAssignableFrom(ret),
                  "'%s' isn't a sub class of '%s'", ret.getClass(),
                  TupleGeneratorBase.class
              );
              return (Class<? extends TupleGeneratorBase>) ret;
            } catch (ClassNotFoundException e) {
              ConfigUtils
                  .rethrow(e, "Failed to instantiate generator '%s'", str);
            }
            // This line will never be executed.
            assert false;
            return null;
          }
        }.withDefaultValue(IPO2TupleGenerator.class),
        new ParamType() {
          @Override protected Object parse(final String[] values) {
            return new Param() {
              @Override public String[] value() {
                return values;
              }

              @Override public Class<? extends Annotation> annotationType() {
                return Param.class;
              }
            };
          }

          @Override public boolean isVarArgs() {
            return true;
          }
        }
    };
  }

  public enum GenerationMode {
    Replay {
      @Override long initializeTuples(RecordedTuplePlayer tupleReplayer,
          Object[] params) {
        File baseDir = Utils
            .baseDirFor((String) params[1], tupleReplayer.getTargetClass());
        final int[] work = new int[] { 0 };
        File[] tupleDirs = tupleReplayer
            .getRecordedTupleDirectories((ReplayMode) params[0],
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
        tupleReplayer.tuples = new TreeMap<Long, Tuple>();
        try {
          for (File dir : tupleDirs) {
            Tuple tuple = TupleUtils.load(
                Utils.openForRead(
                    new File(dir, JCUnitRecorder.TESTCASE_FILENAME)));
            tupleReplayer.tuples
                .put(tupleReplayer.getIdFromDirName(dir.getName()), tuple);
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

      @Override Tuple getTuple(RecordedTuplePlayer tuplePlayer, int tupleId) {
        Utils.checkcond(tuplePlayer.tuples.containsKey((long) tupleId));
        return tuplePlayer.tuples.get((long) tupleId);
      }

      @Override long nextId(RecordedTuplePlayer tuplePlayer, long tupleId) {
        Utils.checkcond(tuplePlayer.tuples.containsKey(tupleId));
        Iterator<Long> tail = tuplePlayer.tuples.tailMap(tupleId).keySet()
            .iterator();
        Utils.checkcond(tail.hasNext());
        tail.next();
        if (!tail.hasNext()) {
          return -1;
        }
        return tail.next();
      }

      @Override long firstId(RecordedTuplePlayer tuplePlayer) {
        if (tuplePlayer.tuples.size() == 0) {
          return -1;
        }
        return tuplePlayer.tuples.firstKey();
      }
    },
    Fallback {
      @SuppressWarnings("unchecked") @Override long initializeTuples(
          RecordedTuplePlayer tupleReplayer,
          Object[] params) {
        ////
        // Create fallbackGenerator instance.
        try {
          tupleReplayer.fallbackGenerator = ((Class<? extends TupleGeneratorBase>) params[2])
              .newInstance();
        } catch (InstantiationException e) {
          ConfigUtils.rethrow(e, "Failed to instantiate '%s'", params[2]);
        } catch (IllegalAccessException e) {
          ConfigUtils.rethrow(e, "Failed to instantiate '%s'", params[2]);
        }
        ////
        // Extract parameters to be passed to fallbackGenerator.
        Param[] paramsToFallbackGenerator;
        if (params.length >= 4) {
          paramsToFallbackGenerator = Arrays
              .copyOfRange(params, 3, params.length, Param[].class);
        } else {
          paramsToFallbackGenerator = new Param[0];
        }
        ////
        // Wire
        TupleGeneratorBase generator = tupleReplayer.fallbackGenerator;
        generator.setFactors(tupleReplayer.getFactors());
        generator.setConstraintManager(tupleReplayer.getConstraintManager());
        generator.setTargetClass(tupleReplayer.getTargetClass());
        generator.init(ConfigUtils.processParams(
            generator.parameterTypes(),
            paramsToFallbackGenerator
        ));
        return generator.size();
      }

      @Override Tuple getTuple(RecordedTuplePlayer tuplePlayer, int tupleId) {
        return tuplePlayer.fallbackGenerator.getTuple(tupleId);
      }

      @Override long nextId(RecordedTuplePlayer tuplePlayer, long tupleId) {
        return tuplePlayer.fallbackGenerator.nextId(tupleId);
      }

      @Override long firstId(RecordedTuplePlayer tuplePlayer) {
        return tuplePlayer.fallbackGenerator.firstId();
      }
    };

    abstract long initializeTuples(RecordedTuplePlayer tupleReplayer,
        Object[] params);

    abstract Tuple getTuple(RecordedTuplePlayer tuplePlayer, int tupleId);

    abstract long nextId(RecordedTuplePlayer tuplePlayer, long tupleId);

    abstract long firstId(RecordedTuplePlayer tuplePlayer);
  }

  public static enum ReplayMode {
    All {
      @Override public boolean shouldBeReplayed(File testStoreDir) {
        return true;
      }
    },
    FailedOnly {
      @Override public boolean shouldBeReplayed(File testStoreDir) {
        return new File(testStoreDir, JCUnitRecorder.FAILED_FILENAME)
            .exists();
      }
    };

    public abstract boolean shouldBeReplayed(File testStoreDir);
  }

  private static interface FoundTupleObserver {
    void found(File f);
  }
}
