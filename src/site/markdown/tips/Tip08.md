# As a pairwise test generator
By creating a test method below, which just prints test case definition, you can use JCUnit as a pairwise (or t-wise) test case generator.

```java

  @Test
  public void printTestCase() {
      System.out.println(TupleUtils.toString(TestCaseUtils.toTestCase(this)));
  }
```

The output will be a text whose lines are JSON objects and look like,

```
{"browser":"IE","cpuClockInGHz":1.5,"edition":"Home Premium","gramInMB":128,"hddSizeInGB":100,"ramInGB":8}
{"browser":"IE","cpuClockInGHz":2.5,"edition":"Enterprise","gramInMB":256,"hddSizeInGB":30,"ramInGB":1}
{"browser":"IE","cpuClockInGHz":3.0,"edition":"Home Basic","gramInMB":512,"hddSizeInGB":50,"ramInGB":2}
{"browser":"Opera","cpuClockInGHz":1.5,"edition":"Ultimate","gramInMB":512,"hddSizeInGB":20,"ramInGB":1}
{"browser":"Opera","cpuClockInGHz":2.0,"edition":"Professional","gramInMB":256,"hddSizeInGB":50,"ramInGB":8}
...
```

You can refer to an example below for this use case.

* [TestGen.java](https://github.com/dakusui/jcunit/tree/develop/src/test/java/com/github/dakusui/jcunit/examples/testgen/TestGen.java)

