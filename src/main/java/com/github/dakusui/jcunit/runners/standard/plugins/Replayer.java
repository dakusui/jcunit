package com.github.dakusui.jcunit.runners.standard.plugins;

import com.github.dakusui.jcunit.core.Checks;
import com.github.dakusui.jcunit.core.IOUtils;
import com.github.dakusui.jcunit.core.SystemProperties;
import com.github.dakusui.jcunit.core.Utils;
import com.github.dakusui.jcunit.core.factor.FactorSpace;
import com.github.dakusui.jcunit.core.factor.Factors;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit.core.tuples.TupleUtils;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArray;
import com.github.dakusui.jcunit.plugins.caengines.CoveringArrayEngine;
import com.github.dakusui.jcunit.plugins.constraints.ConstraintChecker;
import com.github.dakusui.jcunit.runners.core.RunnerContext;
import com.github.dakusui.jcunit.runners.standard.rules.Recorder;

import java.io.File;
import java.io.FilenameFilter;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class Replayer extends CoveringArrayEngine.Base {
  private final GenerationMode            generationMode;
  private final String                    dataDir;
  private final ReplayMode                replayMode;
  private final Class<?>                  testClass;
  private       CoveringArrayEngine       fallbackGenerator;

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
   * @param coveringArrayEngine A tuple generator.
   * @param generationMode      Generation mode. Determines whether let the tuple generator generate a
   *                            new test suite or load it from file system. ("Fallback", "Replay")
   * @param replayMode          Determines if all the test cases or only failed ones in the last run.
   *                            You need to use this is "recorder" rule with this class. ("All", "FailedOnly")
   * @param dataDirName         A directory to store execution data of this class.
   */
  public Replayer(
      @Param(contextKey = RunnerContext.Key.TEST_CLASS, source = Param.Source.RUNNER) Class<?> testClass,
      @Param(source = Param.Source.CONFIG,
          defaultValue = { "com.github.dakusui.jcunit.plugins.caengines.IPO2CAEngine", "2" })
      CoveringArrayEngine coveringArrayEngine,
      @Param(source = Param.Source.CONFIG,
          defaultValue = { "Fallback" })
      GenerationMode generationMode,
      @Param(source = Param.Source.CONFIG,
          defaultValue = { "All" })
      ReplayMode replayMode,
      @Param(source = Param.Source.SYSTEM_PROPERTY,
          propertyKey = SystemProperties.Key.BASEDIR,
          defaultValue = { ".jcunit" })
      String dataDirName
  ) {
    this.testClass = Checks.checknotnull(testClass);
    this.generationMode = Checks.checknotnull(generationMode);
    this.replayMode = Checks.checknotnull(replayMode);
    this.fallbackGenerator = Checks.checknotnull(coveringArrayEngine);
    this.dataDir = Checks.checknotnull(dataDirName);
  }

  /**
   * {@inheritDoc}
   *
   * @param factors
   * @param constraintChecker
   */
  @Override
  protected CoveringArray generate(Factors factors, ConstraintChecker constraintChecker) {
    return this.generationMode.generateCoveringArray(this, factors, constraintChecker);
  }


  private int getIdFromDirName(String dirName) {
    return Integer.parseInt(dirName.substring(dirName.lastIndexOf('-') + 1));
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
      CoveringArray generateCoveringArray(Replayer tupleReplayer, Factors factors, ConstraintChecker constraintChecker) {
        return new CoveringArray.Base(this.initializeTuples(tupleReplayer));
      }

      private List<Tuple> initializeTuples(Replayer tupleReplayer) {
        File baseDir = Recorder.testClassDataDirFor(tupleReplayer.dataDir, tupleReplayer.testClass);
        File[] tupleDirs = tupleReplayer.getRecordedTupleDirectories(tupleReplayer.replayMode,
            baseDir, new FoundTupleObserver() {
              @Override
              public void found(File f) {
              }
            });
        Checks.checktest(tupleDirs != null,
            "Test hasn't been run with 'JCUnitRecorder' rule yet. No tuple containing directory under '%s' was found.",
            baseDir
        );
        List<Tuple> ret = Utils.newList();
        for (File dir : tupleDirs) {
          Tuple tuple = TupleUtils.load(
              IOUtils.openForRead(new File(dir, Recorder.TESTCASE_FILENAME)));
          ret.add(tuple);
        }
        return ret;
      }
    },
    Fallback {
      @Override
      CoveringArray generateCoveringArray(Replayer tupleReplayer, Factors factors, ConstraintChecker constraintChecker) {
        return tupleReplayer.fallbackGenerator.generate(
            new FactorSpace.Builder()
                .addFactorDefs(FactorSpace.convertFactorsIntoSimpleFactorDefs(factors))
                .setTopLevelConstraintChecker(constraintChecker)
                .build());
      }
    };

    abstract CoveringArray generateCoveringArray(Replayer tupleReplayer, Factors factors, ConstraintChecker constraintChecker);
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
