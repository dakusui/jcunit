package com.github.dakusui.jcunit8.experiments.compat;

public class CompatJoinReport {
  final         long timeLhsGeneration;
  final         long timeRhsGeneration;
  final         long timeMergedGeneration;
  final         long timeJoining;
  final         int  sizeLhs;
  final         int  sizeRhs;
  final         int  sizeMerged;
  final         int  sizeJoined;
  private final int  lhsNumFactors;
  private final int  rhsNumFactors;

  public CompatJoinReport(int lhsNumFactors, int rhsNumFactors, long timeLhsGeneration, long timeRhsGeneration, long timeMergedGeneration, long timeJoining, int sizeLhs, int sizeRhs, int sizeMerged, int sizeJoined) {
    this.lhsNumFactors = lhsNumFactors;
    this.rhsNumFactors = rhsNumFactors;
    this.timeLhsGeneration = timeLhsGeneration;
    this.timeRhsGeneration = timeRhsGeneration;
    this.timeMergedGeneration = timeMergedGeneration;
    this.timeJoining = timeJoining;
    this.sizeLhs = sizeLhs;
    this.sizeRhs = sizeRhs;
    this.sizeMerged = sizeMerged;
    this.sizeJoined = sizeJoined;
  }

  public static String header() {
    return "lhsNumFactors,rhsNumFactors,sizeLhs,timeLhsGeneration,sizeRhs,timeRhsGeneration,sizeMerged,timeMergedGeneration,sizeJoined,timeJoining";
  }

  @Override
  public String toString() {
    /*
    return String.format(
        "%d - %d: lhs:[size=%d,time=%d],rhs:[size=%d,time=%d],merged:[size=%d,time%d],joined:[size=%d,time=%d]",
        this.lhsNumFactors, this.rhsNumFactors,
        this.sizeLhs, this.timeLhsGeneration,
        this.sizeRhs, this.timeRhsGeneration,
        this.sizeMerged, this.timeMergedGeneration,
        this.sizeJoined, this.timeJoining
    );
    */
    return String.format(
        "%d,%d,%d,%d,%d,%d,%d,%d,%d,%d",
        this.lhsNumFactors, this.rhsNumFactors,
        this.sizeLhs, this.timeLhsGeneration,
        this.sizeRhs, this.timeRhsGeneration,
        this.sizeMerged, this.timeMergedGeneration,
        this.sizeJoined, this.timeJoining
    );
  }

  public static class Builder {
    private final int  lhsNumFactors;
    private final int  rhsNumFactors;
    private       long timeLhs;
    private       long timeRhs;
    private       long timeMerged;
    private       long timeJoining;
    private       int  sizeLhs;
    private       int  sizeRhs;
    private       int  sizeMerged;
    private       int  sizeJoining;

    public Builder(int lhsNumFactors, int rhsNumFactors) {
      this.lhsNumFactors = lhsNumFactors;
      this.rhsNumFactors = rhsNumFactors;
    }

    public Builder timeLhs(long time) {
      this.timeLhs = time;
      return this;
    }

    public Builder timeRhs(long time) {
      this.timeRhs = time;
      return this;
    }

    public Builder timeMerged(long time) {
      this.timeMerged = time;
      return this;
    }

    public Builder timeJoining(long time) {
      this.timeJoining = time;
      return this;
    }

    public Builder sizeLhs(int size) {
      this.sizeLhs = size;
      return this;
    }

    public Builder sizeRhs(int size) {
      this.sizeRhs = size;
      return this;
    }

    public Builder sizeMerged(int size) {
      this.sizeMerged = size;
      return this;
    }

    public Builder sizeJoining(int size) {
      this.sizeJoining = size;
      return this;
    }

    public CompatJoinReport build() {
      return new CompatJoinReport(
          this.lhsNumFactors,
          this.rhsNumFactors,
          this.timeLhs,
          this.timeRhs,
          this.timeMerged,
          this.timeJoining,
          this.sizeLhs,
          this.sizeRhs,
          this.sizeMerged,
          this.sizeJoining
      );
    }
  }
}
