# Filtering test cases
From time to time, you want to disable/enable test cases in order to concentrate some among all of them.
'@Precondition' notation is your friend in this use case.

You can define a method returning boolean and with no parameter like below,

```java

  @FactorField(intLevels = { 1, 2, 3 })
  public int a;

  ...

  @Precondition
  public boolean filterMethod() {
    return this.a == 3;
  }
```

By defining such a method, you can disable test cases which result in the method's value false.
In the example above, test cases whose factor 'a' is non 3 will no longer be executed.

