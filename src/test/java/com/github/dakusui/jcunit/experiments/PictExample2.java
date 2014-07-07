package com.github.dakusui.jcunit.experiments;

import com.github.dakusui.jcunit.constraints.Constraint;
import com.github.dakusui.jcunit.core.In;
import com.github.dakusui.jcunit.core.In.Domain;
import com.github.dakusui.jcunit.core.JCUnitBase;
import com.github.dakusui.jcunit.compat.core.RuleSet;

public class PictExample2 extends JCUnitBase {
  @In(domain = Domain.Method)
  public String type;

  public static String[] type() {
    return new String[] { "Single", "Spanned", "Striped", "Mirror", "RAID-5" };
  }

  @In(domain = Domain.Method)
  public int size;

  public static int[] size() {
    return new int[] { 10, 100, 1000, 10000, 40000 };
  }

  @In(domain = Domain.Method)
  public String formatMethod;

  public static String[] formatMethod() {
    return new String[] { "Quick", "Slow" };
  }

  @In(domain = Domain.Method)
  public String fileSystem;

  public static String[] fileSystem() {
    return new String[] { "FAT", "FAT32", "NTFS" };
  }

  @In(domain = Domain.Method)
  public int clusterSize;

  public int[] clusterSize() {
    return new int[] { 512, 1024, 2048, 4096, 8192, 16384 };
  }

  @In
  public boolean compression;

  @Constraint
  public RuleSet fileSystemSizeCons = this.ruleSet().incase(eq($("fileSystem"), "FAT"), le($("size"), 4096))
      .incase(eq($("fileSystem"), "FAT32"), le($("size"), 32000));
  @Constraint
  public RuleSet fileSystemCompressionConstraint = this.ruleSet().incase(
      or(ne($("fileSystem"), "NTFS"), and(eq($("fileSystem"), "NTFS"), gt($("clusterSize"), 4096))),
      eq($("compression"), false));
}
