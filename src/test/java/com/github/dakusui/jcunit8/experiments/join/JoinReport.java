package com.github.dakusui.jcunit8.experiments.join;

public class JoinReport {
  private final String lhsDesc;
  private final String rhsDesc;
  private final long   time;
  private final int    size;

  public JoinReport(String lhsDesc, String rhdDesc, int size, long time) {
    this.lhsDesc = lhsDesc;
    this.rhsDesc = rhdDesc;
    this.size = size;
    this.time = time;
  }

  public static String header() {
    return "lhs,rhs,time,size";
  }

  public String toString() {
    return String.format("%s,%s,%s,%s", lhsDesc, rhsDesc, time, size);
  }
}
