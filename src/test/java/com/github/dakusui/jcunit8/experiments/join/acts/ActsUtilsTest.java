package com.github.dakusui.jcunit8.experiments.join.acts;

import com.github.dakusui.peerj.utils.ActsUtils;
import com.github.dakusui.peerj.model.NormalizedConstraint;
import com.github.dakusui.peerj.utils.PeerJUtils;
import org.junit.Test;

import java.io.File;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static com.github.dakusui.crest.Crest.allOf;
import static com.github.dakusui.crest.Crest.asInteger;
import static com.github.dakusui.crest.Crest.asString;
import static com.github.dakusui.crest.Crest.assertThat;
import static com.github.dakusui.crest.Crest.call;
import static com.github.dakusui.peerj.acts.Acts.readTestSuiteFromCsv;

public class ActsUtilsTest {

  @Test
  public void testGenerateAndReport() {
    File baseDir = new File("target");
    ActsUtils.generateAndReport(baseDir, 4, 90, 3);
    ActsUtils.generateAndReport(baseDir, 4, 180, 3);
  }

  @Test
  public void testGenerateAndReportWithConstraints() {
    File baseDir = PeerJUtils.createTempDirectory("target/acts");
    generateAndReportWithConstraints(baseDir, 10, 2);
    generateAndReportWithConstraints(baseDir, 20, 2);
    generateAndReportWithConstraints(baseDir, 30, 2);
    generateAndReportWithConstraints(baseDir, 40, 2);
    generateAndReportWithConstraints(baseDir, 50, 2);
    generateAndReportWithConstraints(baseDir, 60, 2);
    generateAndReportWithConstraints(baseDir, 70, 2);
    generateAndReportWithConstraints(baseDir, 80, 2);
    generateAndReportWithConstraints(baseDir, 90, 2);
    generateAndReportWithConstraints(baseDir, 100, 2);
  }

  @Test
  public void testGenerateAndReportWithConstraintsWithStrength3() {
    File baseDir = PeerJUtils.createTempDirectory("target/acts");
    generateAndReportWithConstraints(baseDir, 10, 3);
    generateAndReportWithConstraints(baseDir, 20, 3);
    generateAndReportWithConstraints(baseDir, 30, 3);
    generateAndReportWithConstraints(baseDir, 40, 3);
  }

  @SuppressWarnings("unchecked")
  private void generateAndReportWithConstraints(File baseDir, int numFactors, int strength) {
    List<Function<List<String>, NormalizedConstraint>> constraints = new LinkedList<>();
    for (int i = 0; i < numFactors / 10; i++) {
      constraints.add(ActsUtils.createBasicConstraint(i * 10));
    }
    ActsUtils.generateAndReport(baseDir, 4, numFactors, strength,
        constraints.toArray(new Function[0])
    );
  }


  @Test
  public void testReadTestSuiteFromCsv() {
    assertThat(
        readTestSuiteFromCsv(readCsv()),
        allOf(
            asInteger("size").eq(10).$(),
            asString(call("get", 0).andThen("get", "PREFIX-0").$()).equalTo("0").$(),
            asString(call("get", 9).andThen("get", "PREFIX-9").$()).equalTo("1").$()
        ));
  }

  private static Stream<String> readCsv() {
    return Stream.of(
        "# ACTS Test Suite Generation: Sun Apr 07 17:57:26 JST 2019",
        "#  '*' represents don't care value ",
        "# Degree of interaction coverage: ",
        "# Number of parameters: 10",
        "# Maximum number of values per parameter: 2",
        "# Number ofconfigurations: 10",
        "PREFIX-0,PREFIX-1,PREFIX-2,PREFIX-3,PREFIX-4,PREFIX-5,PREFIX-6,PREFIX-7,PREFIX-8,PREFIX-9",
        "0,0,1,1,1,1,1,1,1,1",
        "0,1,0,0,0,0,0,0,0,0",
        "1,0,0,1,0,1,0,1,0,1",
        "1,1,1,0,1,0,1,0,1,0",
        "1,0,1,0,0,1,0,0,1,1",
        "1,1,0,1,1,0,1,1,0,0",
        "0,0,1,1,1,0,0,0,0,1",
        "1,1,0,0,0,1,1,1,1,0",
        "1,0,0,1,0,1,1,0,0,0",
        "0,1,1,1,1,1,0,1,0,1");
  }

}
