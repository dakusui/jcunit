package com.github.dakusui.peerj.utils;

import com.github.dakusui.jcunit8.factorspace.FactorSpace;

public enum CasaDataSet {
  BANKING1("IBM", "Banking1"),
  BANKING2("IBM", "Banking2"),
  COMP_PROTOCOL("IBM", "CommProtocol"),
  CONCURRENCY("IBM", "Concurrency"),
  HEALTHCARE1("IBM", "Healthcare1"),
  HEALTHCARE2("IBM", "Healthcare2"),
  HEALTHCARE3("IBM", "Healthcare3"),
  HEALTHCARE4("IBM", "Healthcare4"),
  INSURANCE("IBM", "Insurance"),
  NETWORK_MGMT("IBM", "NetworkMgmt"),
  PROCESSOR_COMM1("IBM", "ProcessorComm1"),
  PROCESSOR_COMM2("IBM", "ProcessorComm2"),
  SERVICES("IBM", "Services"),
  STORAGE1("IBM", "Storage1"),
  STORAGE2("IBM", "Storage2"),
  STORAGE3("IBM", "Storage3"),
  STORAGE4("IBM", "Storage4"),
  STORAGE5("IBM", "Storage5"),
  STORAGE6("IBM", "SystemMgmt"),
  TELECOM("IBM", "Telecom"),
  REAL_APACHE("Real", "benchmark_apache"),
  REAL_BUGZILLA("Real", "benchmark_bugzilla"),
  REAL_GCC("Real", "benchmark_gcc"),
  REAL_SPINS("Real", "benchmark_spins"),
  REAL_SPINV("Real", "benchmark_spinv"),
  TCAS("Real", "tcas"),
  BENCHMARK_1("Synthetic", "benchmark_1"),
  BENCHMARK_2("Synthetic", "benchmark_2"),
  BENCHMARK_3("Synthetic", "benchmark_3"),
  BENCHMARK_4("Synthetic", "benchmark_4"),
  BENCHMARK_5("Synthetic", "benchmark_5"),
  BENCHMARK_6("Synthetic", "benchmark_6"),
  BENCHMARK_7("Synthetic", "benchmark_7"),
  BENCHMARK_8("Synthetic", "benchmark_8"),
  BENCHMARK_9("Synthetic", "benchmark_9"),
  BENCHMARK_10("Synthetic", "benchmark_10"),
  BENCHMARK_11("Synthetic", "benchmark_11"),
  BENCHMARK_12("Synthetic", "benchmark_12"),
  BENCHMARK_13("Synthetic", "benchmark_13"),
  BENCHMARK_14("Synthetic", "benchmark_14"),
  BENCHMARK_15("Synthetic", "benchmark_15"),
  BENCHMARK_16("Synthetic", "benchmark_16"),
  BENCHMARK_17("Synthetic", "benchmark_17"),
  BENCHMARK_18("Synthetic", "benchmark_18"),
  BENCHMARK_19("Synthetic", "benchmark_19"),
  BENCHMARK_20("Synthetic", "benchmark_20"),
  BENCHMARK_21("Synthetic", "benchmark_21"),
  BENCHMARK_22("Synthetic", "benchmark_22"),
  BENCHMARK_23("Synthetic", "benchmark_23"),
  BENCHMARK_24("Synthetic", "benchmark_24"),
  BENCHMARK_25("Synthetic", "benchmark_25"),
  BENCHMARK_26("Synthetic", "benchmark_26"),
  BENCHMARK_27("Synthetic", "benchmark_27"),
  BENCHMARK_28("Synthetic", "benchmark_28"),
  BENCHMARK_29("Synthetic", "benchmark_29"),
  BENCHMARK_30("Synthetic", "benchmark_30"),
  ;

  public final String categoryName;
  public final String modelName;

  CasaDataSet(String categoryName, String modelName) {
    this.categoryName = categoryName;
    this.modelName = modelName;
  }

  public String categoryName() {
    return this.categoryName;
  }

  public String modelName() {
    return this.modelName;
  }

  public static class CasaModel {
    public final int         strength;
    public final FactorSpace factorSpace;

    CasaModel(int strength, FactorSpace factorSpace) {
      this.strength = strength;
      this.factorSpace = factorSpace;
    }

    @Override
    public String toString() {
      return String.format("t=%s;%s", strength, factorSpace);
    }
  }


  public static class NotCombinatorialJoinApplicable extends RuntimeException {
    public NotCombinatorialJoinApplicable(String message) {
      super(message);
    }
  }
}
