![JCUnit](src/main/resources/JCunit-logo.png) is a framework to perform combinatorial tests using 'pairwise'(or more generally 't-wise') 
technique.

# Installation
JCUnit requires Java SE6 or later. 
It is tested using ```JUnit``` (4.12) and ```mockito-core``` (1.9.5).

## Maven coordinate
First of all, you will need to link JCUnit to your project.
Below is a pom.xml fragment to describe jcunit's dependency.
Please add it to your project's pom.xml 

```xml

    <dependency>
      <groupId>com.github.dakusui</groupId>
      <artifactId>jcunit</artifactId>
      <version>[0.6.0,)</version>
    </dependency>
    
```

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
