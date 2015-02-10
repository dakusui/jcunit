#  Writing test cases for error handling (negative tests)
That being said, handling errors appropriately is another concern.
A program must complain of invalid parameters in an appropriate way.
And this characteristic is another aspect of software under test to be tested.

You can do it by overriding 'getViolations' method of a constraint manager and switching the verification procedures based on a test's 'sub-identifier'.

First, by overriding method, return test cases that explicitly violate the constraint represented by the constraint manager class itself.

```java

  public static class CM extends ConstraintManagerBase {
      @Override
      public boolean check(Tuple tuple) throws JCUnitSymbolException {
          ...
      }

      @Override
      public List<Tuple> getViolations() {
          List<Tuple> ret = new LinkedList<Tuple>();
          ret.add(createTestCase("a=0", createTestCase(0, 1, 1)));
          ret.add(createTestCase("b*b-4ca<0", createTestCase(100, 1, 100)));
          ret.add(createTestCase("nonsense 1=0", createTestCase(0, 0, 1)));
          return ret;
      }

      private Tuple createTestCase(int a, int b, int c) {
          return new Tuple.Builder().put("a", a).put("b", b).put("c", c).build();
      }
  }
```

Now you enhance your test case so that it verifies the software under test behaves correctly in case invalid parameters are given.
Let's assume that the SUT should return null if one of the parameters 'a', 'b', or 'c' is not valid.

```java

  @Test
  public void test() {
      if ("a=0".equals(desc.getSubIdentifier())) {
        assertEquals(null, s);
      } else if ("b*b-4ca<0".equals(desc.getSubIdentifier())) {
        assertEquals(null, s);
      } else if ("nonsense 1=0".equals(desc.getSubIdentifier())) {
        assertEquals(null, s);
      } else {
          QuadraticEquationSolver.Solutions s = new QuadraticEquationSolver(a, b,
                  c).solve();
          assertEquals(0.0, a * s.x1 * s.x1 + b * s.x1 + c);
          assertEquals(0.0, a * s.x2 * s.x2 + b * s.x2 + c);
      }
  }
```