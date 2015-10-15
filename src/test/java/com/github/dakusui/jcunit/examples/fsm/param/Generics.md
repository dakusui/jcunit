|種類               |宣言|バインド|?     |境界|  & |example            |
|:----------------:|:--:|:-----:|:----:|:--:|:--:|:-----------------|
|型変数の宣言        |◯   |×      |△*1  |◯   | ◯  |class Hoge&lt;T&gt; {}   |
|型変数へのバインド  |×   |◯      |×     |×   | ×  |new Hoge&lt;String&gt;();|
|変数の型の宣言      |×   |◯      |◯    |◯   | ×  |Hoge&lt;String&gt; a;    |
|型変数での変数宣言  |×   |×      |×     |×   | ×  |T t;               |


```java

    T super B   // not allowed
    T extends B // allowed
    ? super B   // allowed
    ? extends B // allowed
```