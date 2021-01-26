package com.github.dakusui.peerj.ext.acts;

import com.github.dakusui.actionunit.utils.StableTemplatingUtils;
import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.factorspace.FactorSpace;
import com.github.dakusui.peerj.ext.ExternalEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.TreeMap;

import static com.github.dakusui.peerj.ext.acts.ActsUtils.buildActsModel;
import static java.util.Collections.emptyList;
import static java.util.Collections.unmodifiableList;
import static java.util.Objects.requireNonNull;

public class Acts implements ExternalEngine {

  private final FactorSpace factorSpace;
  private final List<Tuple> testCases;
  private final String      algorithm;
  private final String      constraintHandler;
  private final String      mode;

  private static final Logger LOGGER = LoggerFactory.getLogger(Acts.class);
  private final        int    strength;
  private final        File   baseDir;

  private Acts(FactorSpace factorSpace, List<Tuple> testCases, int strength, File baseDir, String algorithm, String mode, String constraintHandler) {
    this.factorSpace = factorSpace;
    this.testCases = unmodifiableList(new ArrayList<>(testCases));
    this.strength = strength;
    this.baseDir = new File(baseDir, Objects.toString(Thread.currentThread().getId()));
    this.algorithm = algorithm;
    this.mode = mode;
    this.constraintHandler = constraintHandler;
  }

  @Override
  public String buildModel() {
    return buildActsModel(factorSpace(), "unknown", testCases());
  }

  @Override
  public List<Tuple> testCases() {
    return this.testCases;
  }

  @Override
  public FactorSpace factorSpace() {
    return this.factorSpace;
  }

  @Override
  public String engineName() {
    return "acts";
  }

  /**
   * <pre>
   * ACTS Version: 3.0
   * Usage: java [options] -jar jarName <inputFileName> [outputFileName]
   * where options include:
   * -Dalgo=ipog|ipog_d|ipof|ipof2|basechoice|null
   *   ipog            - use algorithm IPO (default)
   *   ipog_d          - use algorithm IPO + Binary Construction (for large systems)
   *   ipof            - use ipof method
   *   ipof2           - use the ipof2 method
   *   basechoice      - use Base Choice method
   *   null            - use to check coverage only (no test generation)
   * -Ddoi=<int>
   * specify the degree of interactions to be covered. Use -1 for mixed strength.
   * -Doutput=numeric|nist|csv|excel
   *   numeric         - output test set in numeric format
   *   nist            - output test set in NIST format (default)
   *   csv             - output test set in CSV format
   *   excel           - output test set in EXCEL format
   * -Dmode=scratch|extend
   *   scratch         - generate tests from scratch (default)
   *   extend          - extend from an existing test set
   * -Dchandler=no|solver|forbiddentuples
   *   no              - ignore all constraints
   *   solver          - handle constraints using CSP solver
   *   forbiddentuples - handle constraints using minimum forbidden tuples (default)
   * </pre>
   */
  @Override
  public String composeCommandLine(File inFile, File outFile) {
    return StableTemplatingUtils.template(
        "{{JAVA}} -Ddoi={{STRENGTH}} -Dalgo={{ALGORITHM}} -Dchandler={{CHANDLER}} -Doutput=csv -jar {{ACTS_JAR}} {{IN}} {{OUT}}",
        new TreeMap<String, Object>() {{
          put("{{JAVA}}", "java");
          put("{{STRENGTH}}", strength);
          put("{{ALGORITHM}}", algorithm);
          put("{{MODE}}", mode);
          put("{{CHANDLER}}", constraintHandler);
          put("{{ACTS_JAR}}", actsJar());
          put("{{IN}}", inFile);
          put("{{OUT}}", outFile);
        }});
  }

  @Override
  public File baseDir() {
    return this.baseDir;
  }

  private static String actsJar() {
    return "src/test/resources/bin/acts_3.0.jar";
  }


  public static List<Tuple> runActs(File baseDir, FactorSpace factorSpace, int strength, String algorithm, String mode, String constraintHandler) {
    return runActs(baseDir, factorSpace, strength, algorithm, mode, constraintHandler, emptyList());
  }

  public static List<Tuple> runActs(File baseDir, FactorSpace factorSpace, int strength, String algorithm, String mode, String constraintHandler, List<Tuple> testCases) {
    LOGGER.debug("Directory:{} was created: {}", baseDir, baseDir.mkdirs());
    return new Builder().baseDir(baseDir)
        .factorSpace(factorSpace)
        .strength(strength)
        .algorithm(algorithm)
        .mode(mode)
        .constraintHandler(constraintHandler)
        .testCases(testCases)
        .build()
        .run();
  }

  public static class Builder {
    private File        baseDir;
    private int         strength          = 2;
    private String      algorithm         = "ipog";
    private String      mode              = "scratch";
    private String      constraintHandler = "solver";
    private FactorSpace factorSpace;
    private List<Tuple> testCases         = emptyList();

    public Builder baseDir(File baseDir) {
      this.baseDir = requireNonNull(baseDir);
      return this;
    }

    public Builder strength(int strength) {
      this.strength = strength;
      return this;
    }

    /**
     * You can set one of {@code no}, {@code solver}, and {@code forbiddentuples}, which represent
     * "No constraint handler is used", "CSP solver", and "Minimum forbidden tuples method", respectively.
     *
     * @param constraintHandler A constraint handler used during covering array generation.
     * @return Thi object.
     */
    public Builder constraintHandler(String constraintHandler) {
      this.constraintHandler = requireNonNull(constraintHandler);
      return this;
    }

    /**
     * Sets a name of an algorithm with which a covering array is generated.
     * You can use one of followings.
     * <ul>
     * <li>ipog - use algorithm IPO (default)</li>
     * <li>ipog_d - use algorithm IPO + Binary Construction (for large systems)</li>
     * <li>ipof - use ipof method</li>
     * <li>ipof2 - use the ipof2 method</li>
     * <li>basechoice - use Base Choice method</li>
     * <li>null - use to check coverage only (no test generation)</li>
     * </ul>
     *
     * @param algorithm A name of algorithm used for covering array generation.
     * @return This object
     */
    public Builder algorithm(String algorithm) {
      this.algorithm = requireNonNull(algorithm);
      return this;
    }

    /**
     * - scratch
     * - extend
     *
     * @param mode Generation mode
     * @return This object
     */
    public Builder mode(String mode) {
      this.mode = mode;
      return this;
    }

    public Builder factorSpace(FactorSpace factorSpace) {
      this.factorSpace = requireNonNull(factorSpace);
      return this;
    }

    public Builder testCases(List<Tuple> testCases) {
      this.testCases = requireNonNull(testCases);
      return this;
    }

    public Acts build() {
      return new Acts(
          requireNonNull(factorSpace),
          testCases, strength, baseDir, algorithm, mode, constraintHandler);
    }
  }
}
