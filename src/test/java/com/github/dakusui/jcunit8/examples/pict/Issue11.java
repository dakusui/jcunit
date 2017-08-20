package com.github.dakusui.jcunit8.examples.pict;

import com.github.dakusui.jcunit8.factorspace.Parameter;
import com.github.dakusui.jcunit8.runners.helpers.ParameterUtils;
import com.github.dakusui.jcunit8.runners.junit4.JCUnit8;
import com.github.dakusui.jcunit8.runners.junit4.annotations.From;
import com.github.dakusui.jcunit8.runners.junit4.annotations.ParameterSource;
import org.junit.Test;
import org.junit.runner.RunWith;

import java.util.List;

/**
 * <a href="https://github.com/Microsoft/pict/issues/11">Issue-11 of PICT</a>
 * <code>
 * I'd like to be able to use parameters that are lists or sets.
 * <p>
 * Today I can do something like the following:
 * <p>
 * PLATFORM:   x86, ia64, amd64
 * CPUS:       Single, Dual, Quad
 * RAM:        128MB, 1GB, 4GB, 64GB
 * HDD:        SCSI, IDE
 * OS:         NT4, Win2K, WinXP, Win2K3
 * IE:         4.0, 5.0, 5.5, 6.0
 * APPS_size:  0, 1, 2
 * APPS_0:     n/a, SQLServer, Exchange, Office
 * APPS_1:     n/a, SQLServer, Exchange, Office
 * <p>
 * IF [APPS_size] = 0 THEN [APPS_0] = "n/a" AND [APPS_1] = "n/a";
 * IF [APPS_size] = 1 THEN [APPS_0] <> "n/a" AND [APPS_1] = "n/a";
 * IF [APPS_size] = 2 THEN [APPS_0] <> "n/a" AND [APPS_1] <> "n/a" AND [APPS_0] <> [APPS_1];
 * <p>
 * I'd like to be able to write something more succinct though. e.g.:
 * <p>
 * PLATFORM:   x86, ia64, amd64
 * CPUS:       Single, Dual, Quad
 * RAM:        128MB, 1GB, 4GB, 64GB
 * HDD:        SCSI, IDE
 * OS:         NT4, Win2K, WinXP, Win2K3
 * IE:         4.0, 5.0, 5.5, 6.0
 * APPS (Set): 0..2 of { SQLServer, Exchange, Office }
 * <p>
 * For a list the above example would not have [APPS_0] <> [APPS_1] and would have APPS (List) instead of APPS (Set) (or some similar grammar).
 * </code>
 */
@RunWith(JCUnit8.class)
public class Issue11 {
  @ParameterSource
  public Parameter.Factory<String> platform() {
    return ParameterUtils.simple("x86", "ia16", "amd64");
  }

  @ParameterSource
  public Parameter.Factory<String> cpu() {
    return ParameterUtils.simple("single", "dual", "quad");
  }

  @ParameterSource
  public Parameter.Factory<String> ram() {
    return ParameterUtils.simple("128MB", "1GB", "4GB", "64GB");
  }

  @ParameterSource
  public Parameter.Factory<String> hdd() {
    return ParameterUtils.simple("SCSI", "IDE");
  }

  @ParameterSource
  public Parameter.Factory<String> os() {
    return ParameterUtils.simple("NT4", "Win2K", "WinXP", "Win2K3");
  }

  @ParameterSource
  public Parameter.Factory<String> ie() {
    return ParameterUtils.simple("4.0", "5.0", "5.5", "6.0");
  }

  @ParameterSource
  public Parameter.Factory<List<String>> apps() {
    return ParameterUtils.sequence("SQLServer", "Exchange", "Office").size(3, 4).withoutRepetition().build();
  }

  @Test
  public void test(
      /*/
      @From("platform") String platform,
      @From("cpu") String cpu,
      @From("ram") String ram,
      @From("hdd") String hdd,
      @From("os") String os,
      @From("ie") String ie,
      /*/
      @From("apps") List<Object> app
  ) {
    System.out.println(app);
    //System.out.println(String.format("%s,%s,%s,%s,%s,%s,%s", platform, cpu, ram, hdd, os, ie, app));
  }
}