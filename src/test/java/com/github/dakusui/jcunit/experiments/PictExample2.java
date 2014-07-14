package com.github.dakusui.jcunit.experiments;

import com.github.dakusui.jcunit.compat.core.RuleSet;
import com.github.dakusui.jcunit.compat.core.annotations.In;
import com.github.dakusui.jcunit.compat.core.annotations.In.Domain;
import com.github.dakusui.jcunit.core.Constraint;
import com.github.dakusui.jcunit.core.JCUnitBase;

public class PictExample2 extends JCUnitBase {
  @In(domain = Domain.Method)
  public String  type;
  @In(domain = Domain.Method)
  public int     size;
  @In(domain = Domain.Method)
  public String  formatMethod;
  @In(domain = Domain.Method)
  public String  fileSystem;
  @In(domain = Domain.Method)
  public int     clusterSize;
  @In
  public boolean compression;
  @Constraint
  public RuleSet fileSystemSizeCons              = this.ruleSet()
      .incase(eq($("fileSystem"), "FAT"), le($("size"), 4096))
      .incase(eq($("fileSystem"), "FAT32"), le($("size"), 32000));
  @Constraint
  public RuleSet fileSystemCompressionConstraint = this.ruleSet().incase(
      or(ne($("fileSystem"), "NTFS"),
          and(eq($("fileSystem"), "NTFS"), gt($("clusterSize"), 4096))),
      eq($("compression"), false));

  public static String[] type() {
    return new String[] { "Single", "Spanned", "Striped", "Mirror", "RAID-5" };
  }

  public static int[] size() {
    return new int[] { 10, 100, 1000, 10000, 40000 };
  }

  public static String[] formatMethod() {
    return new String[] { "Quick", "Slow" };
  }

  public static String[] fileSystem() {
    return new String[] { "FAT", "FAT32", "NTFS" };
  }

  public int[] clusterSize() {
    return new int[] { 512, 1024, 2048, 4096, 8192, 16384 };
  }
}
