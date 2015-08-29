# Customizing domains of @FactorField annotated fields (1)
 JCUnit creates test cases by assigning a value, picked up from a hardcoded set of values defined for each type, to each '@FactorField' annotated field in a test class.
 For example, if a member is annotated with '@FactorField' and its type is int, JCUnit will pick up a value from a set
 {1, 0, -1, 100, -100, Integer.MAX_VALUE, Integer.MIN_VALUE}.
 But this set is just a 'default' and you can customize it by using (overriding) an 'xyzLevels' attribute of a '@FactorField' annotation,
 where 'xyz' is a primitive types.

 ```java

     @FactorField(intLevels = { 0, 1, 2, -1, -2, 100, -100, 10000, -10000 })
     public int a;

 ```

 Above is an example of the use the 'intLevels' attribute.
