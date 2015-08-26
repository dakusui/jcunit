# Modeling a FSM

```
Fig. a

    +-----+ cook(a1<a2) +--------+
    |     |------------>|        |-----+
    |  I  |             | Cooked |     |eat(b)
    |     |<------------|        |<----+
    +-----+   done      +--------+

```

If you model this state machine as a Java program for JCUnit, it would be like following.

```java

    public class FlyingSpaghettiMonster {
      private String dish  = null;
      public String cook(int pasta, int sauce) {
        this.dish = pasta;
        return String.format("Cooking %s %s", pasta, sauce);
      }
    
      public String eat() {
        if (dish != null) {
          return String.format("%s is yummy!", this.dish);
        }
        throw new IllegalStateException();
      }
    
      public boolean isReady() {
        return dish != null;
      }
    }
  
```