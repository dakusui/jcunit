# Customizing domains of @FactorField annotated fields (2)
  By using 'levelsProvider' parameter of '@FactorField' and creating a static method whose name is the same as the annotated field's name,
  you can customize the domain of a certain field in a much more flexible way.

  The method mustn't have any parameters and its return value must be an array of the field's type.

  Below is the example for that sort of function.

  ```java

      @FactorField(levelsProvider = MethodLevelsProvider.class)
  	public int a;

  	public static int[] a() {
  		return new int[]{0, 1, 2};
  	}

  ```

  The values returned by the method will be picked up and assigned to the field 'a' by the framework one by one.
  And you need to use 'levelsProvider' attribute when you are going to use non-primitive, non-enum, nor non-string values as levels for a factor.
