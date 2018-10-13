

package com.github.dakusui.jcunit8.experiments;

import com.github.dakusui.jcunit.core.tuples.Tuple;
import com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestName;

import java.util.List;

/**
 * -da -Xms14336m -Xmx14336m
 *
 * [0 2018/10/14T00:27:05JST hiroshi@alexios ~]
 * $ uname -a
 * Linux alexios 4.15.0-36-generic #39-Ubuntu SMP Mon Sep 24 16:19:09 UTC 2018 x86_64 x86_64 x86_64 GNU/Linux
 * [0 2018/10/14T00:27:08JST hiroshi@alexios ~]
 * $ free
 *               total        used        free      shared  buff/cache   available
 * Mem:       16106824     8671636     1323180      613144     6112008     6472548
 * Swap:      32905208           0    32905208
 * [0 2018/10/14T00:27:09JST hiroshi@alexios ~]
 * $ cat /proc/cpuinfo
 * processor	: 0
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3817.688
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 0
 * cpu cores	: 4
 * apicid		: 0
 * initial apicid	: 0
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * processor	: 1
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3844.708
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 1
 * cpu cores	: 4
 * apicid		: 2
 * initial apicid	: 2
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * processor	: 2
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3807.700
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 2
 * cpu cores	: 4
 * apicid		: 4
 * initial apicid	: 4
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * processor	: 3
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3875.232
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 3
 * cpu cores	: 4
 * apicid		: 6
 * initial apicid	: 6
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * processor	: 4
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3838.065
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 0
 * cpu cores	: 4
 * apicid		: 1
 * initial apicid	: 1
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * processor	: 5
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3806.786
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 1
 * cpu cores	: 4
 * apicid		: 3
 * initial apicid	: 3
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * processor	: 6
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3825.073
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 2
 * cpu cores	: 4
 * apicid		: 5
 * initial apicid	: 5
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * processor	: 7
 * vendor_id	: GenuineIntel
 * cpu family	: 6
 * model		: 58
 * model name	: Intel(R) Core(TM) i7-3770K CPU @ 3.50GHz
 * stepping	: 9
 * microcode	: 0x20
 * cpu MHz		: 3802.615
 * cache size	: 8192 KB
 * physical id	: 0
 * siblings	: 8
 * core id		: 3
 * cpu cores	: 4
 * apicid		: 7
 * initial apicid	: 7
 * fpu		: yes
 * fpu_exception	: yes
 * cpuid level	: 13
 * wp		: yes
 * flags		: fpu vme de pse tsc msr pae mce cx8 apic sep mtrr pge mca cmov pat pse36 clflush dts acpi mmx fxsr sse sse2 ss ht tm pbe syscall nx rdtscp lm constant_tsc arch_perfmon pebs bts rep_good nopl xtopology nonstop_tsc cpuid aperfmperf pni pclmulqdq dtes64 monitor ds_cpl vmx est tm2 ssse3 cx16 xtpr pdcm pcid sse4_1 sse4_2 popcnt tsc_deadline_timer aes xsave avx f16c rdrand lahf_lm cpuid_fault epb pti ssbd ibrs ibpb stibp tpr_shadow vnmi flexpriority ept vpid fsgsbase smep erms xsaveopt dtherm ida arat pln pts flush_l1d
 * bugs		: cpu_meltdown spectre_v1 spectre_v2 spec_store_bypass l1tf
 * bogomips	: 7020.33
 * clflush size	: 64
 * cache_alignment	: 64
 * address sizes	: 36 bits physical, 48 bits virtual
 * power management:
 *
 * [0 2018/10/14T00:27:34JST hiroshi@alexios ~]
 * $
 *
 * /usr/lib/jvm/java-8-oracle/bin/java -da -Xms14336m -Xmx14336m -Didea.test.cyclic.buffer.size=1048576 -javaagent:/opt/idea-IC-182.4323.46/lib/idea_rt.jar=41705:/opt/idea-IC-182.4323.46/bin -Dfile.encoding=UTF-8 -classpath /opt/idea-IC-182.4323.46/lib/idea_rt.jar:/opt/idea-IC-182.4323.46/plugins/junit/lib/junit-rt.jar:/opt/idea-IC-182.4323.46/plugins/junit/lib/junit5-rt.jar:/usr/lib/jvm/java-8-oracle/jre/lib/charsets.jar:/usr/lib/jvm/java-8-oracle/jre/lib/deploy.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/cldrdata.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/dnsns.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/jaccess.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/jfxrt.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/localedata.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/nashorn.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/sunec.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/sunjce_provider.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/sunpkcs11.jar:/usr/lib/jvm/java-8-oracle/jre/lib/ext/zipfs.jar:/usr/lib/jvm/java-8-oracle/jre/lib/javaws.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jce.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jfr.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jfxswt.jar:/usr/lib/jvm/java-8-oracle/jre/lib/jsse.jar:/usr/lib/jvm/java-8-oracle/jre/lib/management-agent.jar:/usr/lib/jvm/java-8-oracle/jre/lib/plugin.jar:/usr/lib/jvm/java-8-oracle/jre/lib/resources.jar:/usr/lib/jvm/java-8-oracle/jre/lib/rt.jar:/home/hiroshi/workspace/jcunit/target/test-classes:/home/hiroshi/workspace/jcunit/target/classes:/home/hiroshi/.m2/repository/com/github/dakusui/combinatoradix/0.9.2/combinatoradix-0.9.2.jar:/home/hiroshi/.m2/repository/junit/junit/4.12/junit-4.12.jar:/home/hiroshi/.m2/repository/org/hamcrest/hamcrest-core/1.3/hamcrest-core-1.3.jar:/home/hiroshi/.m2/repository/com/github/dakusui/thincrest/3.5.0/thincrest-3.5.0.jar:/home/hiroshi/.m2/repository/org/mockito/mockito-core/1.9.5/mockito-core-1.9.5.jar:/home/hiroshi/.m2/repository/org/objenesis/objenesis/1.0/objenesis-1.0.jar com.intellij.rt.execution.junit.JUnitStarter -ideVersion5 -junit4 com.github.dakusui.jcunit8.experiments.JoinExperimentOct3
 * T$s0_n3$
 *   time=4[msec]
 *   size=33
 *   width=10
 * ---
 * T$s0_n4$
 *   time=3[msec]
 *   size=33
 *   width=10
 * ---
 * T$s0_n5$
 *   time=2[msec]
 *   size=33
 *   width=10
 * ---
 * T$s1_n3$
 *   time=1301[msec]
 *   size=57
 *   width=30
 * ---
 * T$s1_n4$
 *   time=941[msec]
 *   size=60
 *   width=40
 * ---
 * T$s1_n5$
 *   time=1629[msec]
 *   size=64
 *   width=50
 * ---
 * T$s2_n3$
 *   time=10963[msec]
 *   size=81
 *   width=90
 * ---
 * T$s2_n4$
 *   time=46847[msec]
 *   size=87
 *   width=160
 * ---
 * T$s2_n5$
 *   time=243701[msec]
 *   size=96
 *   width=250
 * ---
 * T$s3_n3$
 *   time=678416[msec]
 *   size=105
 *   width=270
 * ---
 *
 * java.lang.OutOfMemoryError: GC overhead limit exceeded
 *
 * 	at java.util.TreeMap.put(TreeMap.java:540)
 * 	at java.util.AbstractMap.putAll(AbstractMap.java:281)
 * 	at java.util.TreeMap.putAll(TreeMap.java:327)
 * 	at com.github.dakusui.jcunit.core.tuples.Tuple$Builder.build(Tuple.java:56)
 * 	at com.github.dakusui.jcunit.core.tuples.TupleUtils.connectingSubtuplesOf(TupleUtils.java:60)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner$1Session.lambda$null$2(StandardJoiner.java:43)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner$1Session$$Lambda$27/581318631.apply(Unknown Source)
 * 	at java.util.concurrent.ConcurrentHashMap.computeIfAbsent(ConcurrentHashMap.java:1660)
 * 	at com.github.dakusui.jcunit8.core.Utils.lambda$memoize$2(Utils.java:159)
 * 	at com.github.dakusui.jcunit8.core.Utils$$Lambda$16/529116035.apply(Unknown Source)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner$1Session.lambda$findBestCombinationsFor$8(StandardJoiner.java:74)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner$1Session$$Lambda$37/664070838.accept(Unknown Source)
 * 	at java.util.ArrayList.forEach(ArrayList.java:1257)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner$1Session.findBestCombinationsFor(StandardJoiner.java:65)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner$1Session.access$400(StandardJoiner.java:32)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.joiners.StandardJoiner.doJoin(StandardJoiner.java:152)
 * 	at com.github.dakusui.jcunit8.pipeline.stages.Joiner$Base.apply(Joiner.java:19)
 * 	at com.github.dakusui.jcunit8.testutils.testsuitequality.CoveringArrayGenerationUtils.join(CoveringArrayGenerationUtils.java:93)
 * 	at com.github.dakusui.jcunit8.experiments.JoinExperimentOct3.join(JoinExperimentOct3.java:358)
 * 	at com.github.dakusui.jcunit8.experiments.JoinExperimentOct3.T$JOIN(JoinExperimentOct3.java:348)
 * 	at com.github.dakusui.jcunit8.experiments.JoinExperimentOct3.T$s3_n4$(JoinExperimentOct3.java:312)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke0(Native Method)
 * 	at sun.reflect.NativeMethodAccessorImpl.invoke(NativeMethodAccessorImpl.java:62)
 * 	at sun.reflect.DelegatingMethodAccessorImpl.invoke(DelegatingMethodAccessorImpl.java:43)
 * 	at java.lang.reflect.Method.invoke(Method.java:498)
 * 	at org.junit.runners.model.FrameworkMethod$1.runReflectiveCall(FrameworkMethod.java:50)
 * 	at org.junit.internal.runners.model.ReflectiveCallable.run(ReflectiveCallable.java:12)
 * 	at org.junit.runners.model.FrameworkMethod.invokeExplosively(FrameworkMethod.java:47)
 * 	at org.junit.internal.runners.statements.InvokeMethod.evaluate(InvokeMethod.java:17)
 * 	at org.junit.internal.runners.statements.RunBefores.evaluate(RunBefores.java:26)
 * 	at org.junit.rules.TestWatcher$1.evaluate(TestWatcher.java:55)
 * 	at org.junit.rules.RunRules.evaluate(RunRules.java:20)
 *
 *
 * Process finished with exit code 255
 */
public class JoinExperimentOct3 {
  @Rule
  public TestName testName = new TestName();

  private static final boolean DEBUG = false;

  @Before
  public void before() {
    //warm up
    for (int i = 0; i < 10; i++)
      T$JOIN("warmup", 0, 0, 3);
    System.gc();
  }

  @Test
  public void T$s0_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 0, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s1_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 1, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s2_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 2, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s3_n3$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 3, 3);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s0_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 0, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s1_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 1, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s2_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 2, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s3_n4$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 3, 4);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s0_n5$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 0, 5);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s1_n5$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 1, 5);
    printResult(result, System.currentTimeMillis() - before);
  }

  @Test
  public void T$s2_n5$() {
    long before = System.currentTimeMillis();
    List<Tuple> result = T$JOIN("test", 0, 2, 5);
    printResult(result, System.currentTimeMillis() - before);
  }


  private List<Tuple> T$JOIN(String prefix, int i, int s, int n) {
    if (s == 0)
      return testSuiteForComponent(String.format("%s:s=%s[%s]", prefix, s, i));
    String f = prefix + ":s=%s[%s]";
    if (DEBUG)
      System.out.println(String.format("i=%s,s=%s,j=%s/%s", i, s, 0, n));
    List<Tuple> ret = T$JOIN(String.format(f, s, 0), 0, s - 1, n);
    for (int j = 1; j < n; j++) {
      if (DEBUG)
        System.out.println(String.format("i=%s,s=%s,j=%s/%s", i, s, j, n));
      ret = join(ret, T$JOIN(String.format(f, s, j), j, s - 1, n));
    }
    return ret;
  }

  private List<Tuple> testSuiteForComponent(String prefix) {
    return JoinDataSet.load(2, 10, integer -> String.format("%s%03d", prefix, integer));
  }

  private List<Tuple> join(List<Tuple> lhs, List<Tuple> rhs) {
    return CoveringArrayGenerationUtils.join(lhs, rhs, 2);
    //return join(lhs, rhs, 2);
  }

  private void printResult(List<Tuple> result, long duration) {
    printTestInfo(this.testName);
    if (DEBUG)
      result.forEach(System.out::println);
    System.out.println(String.format("  time=%s[msec]", duration));
    System.out.println("  size=" + result.size());
    System.out.println("  width=" + result.get(0).size());
    System.out.println("---");
  }

  private void printTestInfo(TestName testName) {
    System.out.println(testName.getMethodName());
  }
}