![JCUnit](src/main/resources/JCunit-logo.png) is a framework to perform combinatorial tests using 'pairwise'(or more generally 't-wise') 
technique.

# About 0.8.x

I'm planning major chane in 0.8.x including move to Java 8, redesigning
pipeline mechanism, etc., most of which are incompatible with older versions.

[![Build Status](https://travis-ci.org/dakusui/jcunit.svg?branch=0.7.x-develop)](https://travis-ci.org/dakusui/jcunit)
[![codecov.io](https://codecov.io/github/dakusui/jcunit/coverage.svg?branch=0.7.x-develop)](https://codecov.io/github/dakusui/jcunit?branch=0.7.x-develop)

# Installation
JCUnit(8) requires Java SE8 or later.
It is tested using ```JUnit``` (4.12) and ```mockito-core``` (1.9.5).

You will only need to link JCUnit to your project.
Below is a pom.xml fragment to describe jcunit's dependency.
Please add it to your project's pom.xml

```xml

    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>jcunit</artifactId>
      <version>[0.7.0,)</version>
      <scope>test</scope>
    </dependency>
    <dependency>
      <groupId>junit</groupId>
      <artifactId>junit</artifactId>
      <version>4.12</version>
      <scope>test</scope>
    </dependency>
```

That's it. Let's go.

**NOTE:** Please use JCUnit with JUnit 4.12 (or later). Otherwise JCUnit will not be able to execute tests for failing to instantiate parameterized runner.

# References

* [JCUnit wiki](https://github.com/dakusui/jcunit/wiki)
* [JCUnit blog; Japanese; 日本語](http://jcunit.hatenablog.jp/)

# Copyright and license #

Copyright 2013 Hiroshi Ukai.

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this work except in compliance with the License.
You may obtain a copy of the License in the LICENSE file, or at:

  [http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
